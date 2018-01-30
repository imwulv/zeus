/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.f6car.base.web.interceptor;

import com.air.tqb.vo.TbUserVo;
import com.f6car.base.constant.Constants;
import com.f6car.base.core.F6Static;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

import static com.f6car.base.constant.Constants.SESSION_ATTRIBUTE_NAME_USER;

/**
 * @author qixiaobo
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
@Component
public class CleanInterceptor extends AbstractExcludeInterceptor {

    private static final List<String> POSSIBLE_IP_HEADER = Lists.newArrayList("x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR");

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
        F6Static.clearAll();
    }

    private static String getIpAddress(HttpServletRequest request) {
        String ip = null;
        for (String ipHeader : POSSIBLE_IP_HEADER) {
            ip = request.getHeader(ipHeader);
            if (!Strings.isNullOrEmpty(ip) && !Constants.IP_UNKNOWN.equalsIgnoreCase(ip)) {
                break;
            } else {
                ip = null;
            }
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.indexOf(Constants.COMMA) != -1) {
            ip = ip.substring(0, ip.indexOf(Constants.COMMA));
        }
        return ip;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            TbUserVo user = (TbUserVo) session.getAttribute(SESSION_ATTRIBUTE_NAME_USER);
            if (user != null) {
                F6Static.setOrg(user.getIdOwnOrg());
                F6Static.setUser(user.getPkId());
            }
        }
        F6Static.setType(F6Static.ActionType.WEB);
        F6Static.setAction(request.getRequestURI());
        F6Static.setIp(getIpAddress(request));
        return super.preHandle(request, response, handler);
    }
}
