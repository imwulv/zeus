/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.f6car.base.config;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.f6car.base.common.Result;
import com.f6car.base.common.ResultCode;
import com.f6car.base.constant.Constants;
import com.f6car.base.exception.IllegalAccessException;
import com.f6car.base.exception.ServiceException;
import com.f6car.base.web.converter.ExcelHttpMessageConverter;
import com.f6car.base.web.interceptor.ExcludePathable;
import com.f6car.base.web.json.BigIntegerValueFilter;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import static com.f6car.base.web.converter.ExcelHttpMessageConverter.EXCEL_MEDIA_TYPE;

/**
 * @author qixiaobo
 */
@Configuration
@Lazy
public class WebMvcConfigurer extends WebMvcConfigurerAdapter {


    private final Logger logger = LoggerFactory.getLogger(WebMvcConfigurer.class);
    @Autowired
    private List<HandlerInterceptor> interceptorList = Collections.emptyList();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        List<ResourceHandler> resourceHandlerList = resourceHandlerConfig().getResourceHandlerList();
        for (ResourceHandler resourceHandler : resourceHandlerList) {
            registry.addResourceHandler(resourceHandler.getPattern())
                    .addResourceLocations(resourceHandler.getLocation());
        }

    }

    @Bean
    public ResourceHandlerConfig resourceHandlerConfig() {
        return new ResourceHandlerConfig();
    }

    //统一异常处理
    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        exceptionResolvers.add(new HandlerExceptionResolver() {
            @Override
            public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
                Result result = new Result();
                if (e instanceof ServiceException) {
                    result.setCode(ResultCode.FAIL).setMessage(e.getMessage());
                    logger.info(e.getMessage());
                } else if (e instanceof NoHandlerFoundException) {
                    result.setCode(ResultCode.NOT_FOUND).setMessage("接口 [" + request.getRequestURI() + "] 不存在");
                } else if (e instanceof ServletException) {
                    result.setCode(ResultCode.FAIL).setMessage(e.getMessage());
                } else if (e instanceof AuthorizationException) {
                    result.setCode(ResultCode.FORBIDDEN).setMessage(e.getMessage());
                } else if (e instanceof AuthenticationException || e instanceof IllegalAccessException) {
                    result.setCode(ResultCode.UNAUTHORIZED).setMessage(e.getMessage());
                } else {
                    result.setCode(ResultCode.INTERNAL_SERVER_ERROR).setMessage("接口 [" + request.getRequestURI() + "] 内部错误，请联系管理员");
                    String message;
                    if (handler instanceof HandlerMethod) {
                        HandlerMethod handlerMethod = (HandlerMethod) handler;
                        message = String.format("接口 [%s] 出现异常，方法：%s.%s，异常摘要：%s",
                                request.getRequestURI(),
                                handlerMethod.getBean().getClass().getName(),
                                handlerMethod.getMethod().getName(),
                                e.getMessage());
                    } else {
                        message = e.getMessage();
                    }
                    logger.error(message, e);
                }
                responseResult(response, result);
                return new ModelAndView();
            }

        });
    }

    //解决跨域问题
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }

    //添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        for (HandlerInterceptor handlerInterceptor : interceptorList) {
            logger.info("add interceptor " + handlerInterceptor.getClass());
            InterceptorRegistration interceptorRegistration = registry.addInterceptor(handlerInterceptor);
            if (handlerInterceptor instanceof ExcludePathable) {
                List<String> excludePath = ((ExcludePathable) handlerInterceptor).getExcludePath();
                if (!excludePath.isEmpty()) {
                    interceptorRegistration.excludePathPatterns(excludePath.toArray(new String[excludePath.size()]));
                }
            }
            interceptorRegistrationExcluedStaticCallback(interceptorRegistration);
        }
    }


    private void interceptorRegistrationExcluedStaticCallback(InterceptorRegistration interceptorRegistration) {
        List<ResourceHandler> resourceHandlerList = resourceHandlerConfig().getResourceHandlerList();
        for (ResourceHandler resourceHandler : resourceHandlerList) {
            interceptorRegistration.excludePathPatterns(resourceHandler.getPattern());
        }
        interceptorRegistration.excludePathPatterns("/webjars/**", "/swagger-ui.html", "/error", "/v2/**");
    }

    private void responseResult(HttpServletResponse response, Result result) {
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        response.setStatus(200);
        try {
            response.getWriter().write(JSON.toJSONString(result));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }


    static final Splitter COMMA_SPLITTER = Splitter.on(Constants.COMMA);

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(createExcelHttpMessageConverter());
        converters.add(createFastJsonHttpMessageConverter());
    }

    private FastJsonHttpMessageConverter createFastJsonHttpMessageConverter() {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        FastJsonConfig config = new FastJsonConfig();
        config.setSerializerFeatures(SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteNullNumberAsZero);
        config.setSerializeFilters(serializeFilters());
        converter.setFastJsonConfig(config);
        converter.setDefaultCharset(Charset.forName("UTF-8"));
        return converter;
    }

    @Bean
    public SerializeFilter[] serializeFilters() {
        return new SerializeFilter[]{new BigIntegerValueFilter()};
    }

    private HttpMessageConverter<Object> createExcelHttpMessageConverter() {
        return excelHttpMessageConverter();
    }

    @Bean
    public ExcelHttpMessageConverter excelHttpMessageConverter() {
        return new ExcelHttpMessageConverter();
    }


    @Bean
    public ViewResolver contentNegotiatingViewResolver(
            ContentNegotiationManager manager) {
        // Define the view resolvers
        ViewResolver beanNameViewResolver = new BeanNameViewResolver();
        List<ViewResolver> resolvers = Lists.newArrayList(beanNameViewResolver);


        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
        resolver.setViewResolvers(resolvers);
        resolver.setContentNegotiationManager(manager);
        return resolver;
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(true)
                .useJaf(false)
                .favorParameter(true)
                .parameterName("format")
                .ignoreAcceptHeader(true)
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("xls", EXCEL_MEDIA_TYPE);
    }


}
