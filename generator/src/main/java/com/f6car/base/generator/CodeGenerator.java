/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.f6car.base.generator;

import com.google.common.base.*;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.joda.time.DateTime;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static com.f6car.base.constant.Constants.*;

/**
 * @author qixiaobo
 */
public class CodeGenerator {
    private static final String JDBC_URL = "jdbc:mysql://local-test.db.f6car:3306/f6dms_20160522";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "root";
    private static final String JDBC_DIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

    public static final ThreadLocal<List<IntrospectedColumn>> PO_FIELDS = new ThreadLocal<>();
    public static final ThreadLocal<FullyQualifiedJavaType> PK_TYPE = new ThreadLocal<>();
    private static final String PROJECT_PATH = System.getProperty("user.dir");
    private static final String TEMPLATE_FILE_PATH = PROJECT_PATH + "/src/test/resources/template";
    private static final String JAVA_PATH = "/src/main/java";
    private static final String TEST_JAVA_PATH = "/src/test/java";
    private static final String RESOURCES_PATH = "/src/main/resources";
    private static final String PACKAGE_PATH_SERVICE = packageConvertPath(SERVICE_PACKAGE);
    private static final String PACKAGE_PATH_SERVICE_IMPL = packageConvertPath(SERVICE_IMPL_PACKAGE);
    private static final String PACKAGE_PATH_SO = packageConvertPath(SO_PACKAGE);
    private static final String PACKAGE_PATH_VO = packageConvertPath(VO_PACKAGE);
    private static final String PACKAGE_PATH_CONTROLLER = packageConvertPath(CONTROLLER_PACKAGE);
    private static final String PACKAGE_PATH_TEST = packageConvertPath(TEST_PACKAGE);
    private static final String AUTHOR = "qixiaobo";
    private static final Splitter TABLE_NAME_SPLITTER = Splitter.on(UNDER_LINE).trimResults().omitEmptyStrings();
    private static final String DATE = new DateTime().toString("yyyy-MM-dd");
    private static final List<String> VO_EXCLUED_FIELD_NAMES = Lists.newArrayList("creator", "modifier", "modifiedtime", "creationtime");
    private static final Joiner PATH_JOINER = Joiner.on("/");
    private static GroupTemplate gt;

    static {
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader("template");
        org.beetl.core.Configuration cfg = null;
        try {
            cfg = org.beetl.core.Configuration.defaultConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
        }
        gt = new GroupTemplate(resourceLoader, cfg);
    }

    public static void main(String[] args) throws IOException {
        genCode("tb_user", "tb_org_mac", "ts_maintain");
    }

    /**
     * 通过数据表名称生成代码，Model 名称通过解析数据表名称获得，下划线转大驼峰的形式。
     * 如输入表名称 "t_user_detail" 将生成 TUserDetail、TUserDetailMapper、TUserDetailService ...
     *
     * @param tableNames 数据表名称...
     */
    public static void genCode(String... tableNames) throws IOException {
        for (String tableName : tableNames) {
            genCodeByCustomModelName(tableName, null);
        }
    }

    /**
     * 通过数据表名称，和自定义的 Model 名称生成代码
     * 如输入表名称 "t_user_detail" 和自定义的 Model 名称 "User" 将生成 User、UserMapper、UserService ...
     *
     * @param tableName 数据表名称
     * @param modelName 自定义的 Model 名称
     */
    public static void genCodeByCustomModelName(String tableName, String modelName) throws IOException {
        String subPackage = "";
        List<String> strings = TABLE_NAME_SPLITTER.splitToList(tableName);
        if (strings.size() >= 2) {
            if (strings.get(0).length() <= 2) {
                subPackage = strings.get(1);
            }
        }

        genModelAndMapper(tableName, modelName, subPackage);
        genService(tableName, modelName, subPackage);
        genControllerAndTest(tableName, modelName, subPackage);
    }


    public static void genModelAndMapper(String tableName, String modelName, String subPackageName) {
        Context context = new Context(ModelType.FLAT);
        context.setId("f6car");
        context.setTargetRuntime("MyBatis3");
        context.addProperty(PropertyRegistry.CONTEXT_BEGINNING_DELIMITER, "`");
        context.addProperty(PropertyRegistry.CONTEXT_ENDING_DELIMITER, "`");

        JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();
        jdbcConnectionConfiguration.setConnectionURL(JDBC_URL);
        jdbcConnectionConfiguration.setUserId(JDBC_USERNAME);
        jdbcConnectionConfiguration.setPassword(JDBC_PASSWORD);
        jdbcConnectionConfiguration.setDriverClass(JDBC_DIVER_CLASS_NAME);
        context.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);
        JavaTypeResolverConfiguration javaTypeResolverConfiguration = new JavaTypeResolverConfiguration();
        javaTypeResolverConfiguration.setConfigurationType(F6JavaTypeResolverDefaultImpl.class.getName());
        context.setJavaTypeResolverConfiguration(javaTypeResolverConfiguration);
        PluginConfiguration pluginConfiguration = new PluginConfiguration();
        pluginConfiguration.setConfigurationType(F6MapperPlugin.class.getName());
        pluginConfiguration.addProperty("mappers", MAPPER_INTERFACE_REFERENCE);
        context.addPluginConfiguration(pluginConfiguration);

        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetProject(PROJECT_PATH + Module.model.moduleDir + JAVA_PATH);
        String javaPackage = MODEL_PACKAGE;
        String mapperPackage = MAPPER_PACKAGE;
        String xmlPackage = "mapper";
        if (!Strings.isNullOrEmpty(subPackageName)) {
            javaPackage += ("." + subPackageName);
            mapperPackage += ("." + subPackageName);
            xmlPackage += ("/" + subPackageName);
        }
        javaModelGeneratorConfiguration.setTargetPackage(javaPackage);
        context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
        sqlMapGeneratorConfiguration.setTargetProject(PROJECT_PATH + Module.dao.moduleDir + RESOURCES_PATH);
        sqlMapGeneratorConfiguration.setTargetPackage(xmlPackage);
        context.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);

        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = new JavaClientGeneratorConfiguration();
        javaClientGeneratorConfiguration.setTargetProject(PROJECT_PATH + Module.dao.moduleDir + JAVA_PATH);
        javaClientGeneratorConfiguration.setTargetPackage(mapperPackage);
        javaClientGeneratorConfiguration.setConfigurationType("XMLMAPPER");
        context.setJavaClientGeneratorConfiguration(javaClientGeneratorConfiguration);

        TableConfiguration tableConfiguration = new TableConfiguration(context);
        tableConfiguration.setTableName(tableName);
        if (!Strings.isNullOrEmpty(modelName)) {
            tableConfiguration.setDomainObjectName(modelName);
        }
        tableConfiguration.setGeneratedKey(new GeneratedKey("id", "Mysql", true, null));
        context.addTableConfiguration(tableConfiguration);

        List<String> warnings;
        MyBatisGenerator generator;
        try {
            Configuration config = new Configuration();
            config.addContext(context);
            config.validate();

            boolean overwrite = true;
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            warnings = new ArrayList<>();
            generator = new MyBatisGenerator(config, callback, warnings);
            generator.generate(null);
        } catch (Exception e) {
            throw new RuntimeException("生成Model和Mapper失败", e);
        }

        if (generator.getGeneratedJavaFiles().isEmpty() || generator.getGeneratedXmlFiles().isEmpty()) {
            throw new RuntimeException("生成Model和Mapper失败：" + warnings);
        }
        if (StringUtils.isEmpty(modelName)) {
            modelName = tableNameConvertUpperCamel(tableName);
        }
        System.out.println(modelName + ".java 生成成功");
        System.out.println(modelName + "Mapper.java 生成成功");
        System.out.println(modelName + "Mapper.xml 生成成功");
    }

    public static void genService(String tableName, String modelName, String subPackage) throws IOException {

        Map<String, Object> data = new HashMap<>();
        data.put("date", DATE);
        data.put("author", AUTHOR);
        String modelNameUpperCamel = StringUtils.isEmpty(modelName) ? tableNameConvertUpperCamel(tableName) : modelName;
        data.put("modelNameUpperCamel", modelNameUpperCamel);
        data.put("modelNameLowerCamel", tableNameConvertLowerCamel(tableName));
        data.put("basePackage", BASE_PACKAGE);
        data.put("pk", PK_TYPE.get());
        String servicePackageDir = PROJECT_PATH + Module.service.moduleDir + JAVA_PATH + PACKAGE_PATH_SERVICE;
        String serviceImplPackageDir = PROJECT_PATH + Module.serviceimpl.moduleDir + JAVA_PATH + PACKAGE_PATH_SERVICE_IMPL;
        String soPackageDir = PROJECT_PATH + Module.dto.moduleDir + JAVA_PATH + PACKAGE_PATH_SO;
        String voPackageDir = PROJECT_PATH + Module.dto.moduleDir + JAVA_PATH + PACKAGE_PATH_VO;
        if (!Strings.isNullOrEmpty(subPackage)) {
            data.put("subPackage", "." + subPackage);
            servicePackageDir += ("/" + subPackage + "/");
            serviceImplPackageDir += ("/" + subPackage + "/");
            soPackageDir += ("/" + subPackage + "/");
            voPackageDir += ("/" + subPackage + "/");
        }


        generateByTemplate(gt, "/service.beetl", servicePackageDir + modelNameUpperCamel + "Service.java", data);
        generateByTemplate(gt, "/service-impl.beetl", serviceImplPackageDir + modelNameUpperCamel + "ServiceImpl.java", data);
        data.put("fields", getVoFields());
        generateByTemplate(gt, "/so.beetl", soPackageDir + modelNameUpperCamel + "So.java", data);
        generateByTemplate(gt, "/vo.beetl", voPackageDir + modelNameUpperCamel + "Vo.java", data);


    }

    private static Collection<IntrospectedColumn> getVoFields() {
        if (PO_FIELDS.get().isEmpty()) {
            return Collections.emptyList();
        } else {
            return Collections2.filter(PO_FIELDS.get(), new Predicate<IntrospectedColumn>() {
                @Override
                public boolean apply(IntrospectedColumn input) {
                    if (VO_EXCLUED_FIELD_NAMES.contains(input.getJavaProperty())) {
                        return false;
                    }
                    return true;
                }
            });
        }
    }

    private static void generateByTemplate(GroupTemplate groupTemplate, String templateFile, String filename, Map data) throws IOException {
        File serviceFile = new File(filename);
        if (!serviceFile.getParentFile().exists()) {
            serviceFile.getParentFile().mkdirs();
        }
        Template template = groupTemplate.getTemplate(templateFile);
        template.binding(data);
        template.renderTo(new FileWriter(serviceFile));

        System.out.println(filename + "生成成功");
    }

    public static void genControllerAndTest(String tableName, String modelName, String subPackage) throws IOException {

        Map<String, Object> data = new HashMap<>();
        data.put("date", DATE);
        data.put("author", AUTHOR);
        data.put("pk", PK_TYPE.get());
        String modelNameUpperCamel = StringUtils.isEmpty(modelName) ? tableNameConvertUpperCamel(tableName) : modelName;
        List<String> strings = TABLE_NAME_SPLITTER.splitToList(tableName.toLowerCase());
        if (strings.size() >= 2) {
            if (strings.get(0).length() <= 2) {
                strings = Lists.newArrayList(strings);
                strings.remove(0);
            }
            data.put("baseRequestMapping", PATH_JOINER.join(strings));
        }
        if (!data.containsKey("baseRequestMapping")) {
            data.put("baseRequestMapping", modelNameConvertMappingPath(modelNameUpperCamel));
        }
        data.put("modelNameUpperCamel", modelNameUpperCamel);
        data.put("modelNameLowerCamel", CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, modelNameUpperCamel));
        data.put("basePackage", BASE_PACKAGE);
        String controllerPackageDir = PROJECT_PATH + Module.web.moduleDir + JAVA_PATH + PACKAGE_PATH_CONTROLLER;
        String testPackageDir = PROJECT_PATH + Module.web.moduleDir + TEST_JAVA_PATH + PACKAGE_PATH_TEST;
        if (!Strings.isNullOrEmpty(subPackage)) {
            data.put("subPackage", "." + subPackage);
            controllerPackageDir += ("/" + subPackage + "/");
            testPackageDir += ("/" + subPackage + "/");
        }


        generateByTemplate(gt, "/controller.beetl", controllerPackageDir + modelNameUpperCamel + "Controller.java", data);
        generateByTemplate(gt, "/test.beetl", testPackageDir + modelNameUpperCamel + "ControllerTest.java", data);
        PO_FIELDS.remove();
    }

    private static String tableNameConvertLowerCamel(String tableName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, tableName.toLowerCase());
    }

    private static String tableNameConvertUpperCamel(String tableName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName.toLowerCase());

    }

    private static String tableNameConvertMappingPath(String tableName) {
        tableName = tableName.toLowerCase();
        return "/" + (tableName.contains("_") ? tableName.replaceAll("_", "/") : tableName);
    }

    private static String modelNameConvertMappingPath(String modelName) {
        String tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, modelName);
        return tableNameConvertMappingPath(tableName);
    }

    private static String packageConvertPath(String packageName) {
        return String.format("/%s/", packageName.contains(".") ? packageName.replaceAll("\\.", "/") : packageName);
    }

    private static enum Module {
        dao("/dao"), web("/web"), service("/service"), serviceimpl("/serviceimpl"), dto("/dto"), model("/model");
        private String moduleDir;

        Module(String moduleDir) {
            this.moduleDir = moduleDir;
        }
    }
}
