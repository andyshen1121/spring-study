package com.study.spring.framework.servlet;

import com.study.spring.framework.annotation.*;
import com.study.spring.framework.context.GPApplicationContext;


import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class GPDispatcherServlet extends HttpServlet {

    // 保存用户配置好的配置文件
//    private Properties contextConfig = new Properties();

    // 缓存从包路径下扫描的全类名
//    private List<String> classNames = new ArrayList<>();

    // 保存所有扫描的类的实例
//    private Map<String, Object> ioc = new HashMap<>();

    // 保存Controller里面URL和Method的对应关系
    private Map<String, Method> handlerMapping = new HashMap<>();

    // IoC容器的访问上下文
    private GPApplicationContext applicationContext = null;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // 6. 根据URL委派给具体的调用方法
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception, Detail: " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
//        如果项目名称为test,你在浏览器中输入请求路径：http://localhost:8080/test/pc/list.jsp
//        执行下面向行代码后打印出如下结果：
//        1、 System.out.println(request.getContextPath());
//        打印结果：/test
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found.");
            return;
        }
        Method method = this.handlerMapping.get(url);


        // 1. 先把形参的位置和参数名字建立映射关系，并且缓存下来
        Map<String, Integer> paramIndexMapping = new HashMap<>();
        Annotation[][] pa = method.getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            for (Annotation a : pa[i]) {
                if (a instanceof GPRequestParam) {
                    String paramName = ((GPRequestParam) a).value();
                    if (!"".equals(paramName.trim())) {
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }
        }

        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> type = paramTypes[i];
            if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                paramIndexMapping.put(type.getName(), i);
            }
        }
        // 2. 根据参数位置匹配名字，从url中取到参数名字对应的值
        Object[] paramValues = new Object[paramTypes.length];

        //http://localhost/demo/query?name=Tom&name=Tomcat&name=Mic
        Map<String, String[]> params = req.getParameterMap();
        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "")
                    .replaceAll("\\s", "");
            if (!paramIndexMapping.containsKey(param.getKey())) {
                continue;
            }
            int index = paramIndexMapping.get(param.getKey());
            paramValues[index] = value;
        }
        if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = resp;
        }

        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(applicationContext.getBean(beanName), paramValues);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
//        // 1. 加载配置文件
//        doLoadConfig(config.getInitParameter("contextConfigLocation"));
//
//        // 2. 扫描相关的类
//        doScanner(contextConfig.getProperty("scanPackage"));
//
//        // ========IoC==========
//        // 3. 初始化IoC容器，并且将扫描到的类进行实例化，缓存到IoC容器中
//        doInstance();
//
//        // ========DI==========
//        // 4. 完成依赖注入
//        doAutowired();

        applicationContext = new GPApplicationContext(config.getInitParameter("contextConfigLocation"));

        // =======MVC功能=========
        // 5. 初始化HandlerMapping
        doInitHandlerMapping();

        System.out.println("Spring framework is init.");


    }

    private void doInitHandlerMapping() {
        if (this.applicationContext.getBeanDefinitionCount() == 0) {
            return;
        }
        for (String beanName : this.applicationContext.getBeanDefinitionNames()) {
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();
            if (!clazz.isAnnotationPresent(GPController.class)) {
                continue;
            }
            String baseUrl = "";
            if (clazz.isAnnotationPresent(GPRequestMapping.class)) {
                GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
                baseUrl = requestMapping.value();
            }
            // 只迭代public方法
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(GPRequestMapping.class)) {
                    continue;
                }
                GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);
                System.out.println("Mapped: " + url + "--> " + method);
            }
        }
    }

//    private void doAutowired() {
//        if (ioc.isEmpty()) {
//            return;
//        }
//        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
//            // 忽略字段的修饰符，不管你是private、protected、public、default
//            for (Field field : entry.getValue().getClass().getDeclaredFields()) {
//                if (!field.isAnnotationPresent(GPAutowired.class)) {
//                    continue;
//                }
//                GPAutowired autowired = field.getAnnotation(GPAutowired.class);
//                String beanName = autowired.value().trim();
//                if ("".equals(beanName)) {
//                    beanName = field.getType().getName();
//                }
//                field.setAccessible(true);
//                try {
//                    field.set(entry.getValue(), ioc.get(beanName));
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

//    private void doInstance() {
//        if (classNames.isEmpty()) {
//            return;
//        }
//        for (String className : classNames) {
//            try {
//                Class<?> clazz = Class.forName(className);
//                if (clazz.isAnnotationPresent(GPController.class)) {
//                    String beanName = toLowerFirstCase(clazz.getSimpleName());
//                    Object instance = clazz.newInstance();
//                    ioc.put(beanName, instance);
//                } else if (clazz.isAnnotationPresent(GPService.class)) {
//                    // 1. 默认类名首字母小写
//                    String beanName = toLowerFirstCase(clazz.getSimpleName());
//
//                    // 2. 如果在多个包下出现了相同的类名，优先使用别名（自定义命名）
//                    GPService service = clazz.getAnnotation(GPService.class);
//                    if (!"".equals(service.value())) {
//                        beanName = service.value();
//                    }
//                    Object instance = clazz.newInstance();
//                    ioc.put(beanName, instance);
//
//                    // 3. 如果是接口，只能初始化它的实现类
//                    for (Class<?> i : clazz.getInterfaces()) {
//                        if (ioc.containsKey(i.getName())) {
//                            throw new Exception("The " + i.getName() + " exists!");
//                        }
//                        ioc.put(i.getName(), instance);
//                    }
//
//                } else {
//                    continue;
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    // 扫描classPath下符合包路径下
//    private void doScanner(String scanPackage) {
//        // 把点替换成/
//        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
//        File classPath = new File(url.getFile());
//        // 获取路径下所有类名
//        for (File file : classPath.listFiles()) {
//            if (file.isDirectory()) {
//                doScanner(scanPackage + "." + file.getName());
//            } else {
//                // 取反，可以减少代码嵌套
//                if (!file.getName().endsWith(".class")) {
//                    continue;
//                }
//                // 包名.类名
//                String className = (scanPackage + "." + file.getName().replace(".class", ""));
//                // 实例化 要用到Class.forName(className);
//                classNames.add(className);
//            }
//
//        }
//    }

//    private void doLoadConfig(String contextConfigLocation) {
//        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
//        try {
//            contextConfig.load(is);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (null != is) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//    }
}
