package com.study.spring.framework.support;

import com.study.spring.framework.config.GPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GPBeanDefinitionReader {

    // 保存用户配置好的配置文件
    private Properties contextConfig = new Properties();

    // 缓存从包路径下扫描的全类名
    private List<String> registryBeanClasses = new ArrayList<>();

    public GPBeanDefinitionReader(String ... locations) {
        // 1、加载Properties文件
        doLoadConfig(locations[0]);

        // 2、扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
    }

    public List<GPBeanDefinition> loadBeanDefinitions() {

        List<GPBeanDefinition> result = new ArrayList<>();
        for (String className : registryBeanClasses) {
            try {
                Class<?> beanClass = Class.forName(className);

                // beanClass本身是接口的话，不做处理
                if (beanClass.isInterface()) {
                    continue;
                }
                // 1. 默认类名首字母小写的情况
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));

                // 2. 如果是接口，就用实现类
                for (Class<?> i : beanClass.getInterfaces()) {
                    result.add(doCreateBeanDefinition(i.getName(), beanClass.getName()));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    private GPBeanDefinition doCreateBeanDefinition(String factoryBeanName, String factoryClassName) {

        GPBeanDefinition beanDefinition = new GPBeanDefinition();
        beanDefinition.setBeanClassName(factoryClassName);
        beanDefinition.setFactoryBeanName(factoryBeanName);
        return beanDefinition;
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation.replaceAll("classpath:", ""));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 扫描classPath下符合包路径下
    private void doScanner(String scanPackage) {
        // 把点替换成/
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        // 获取路径下所有类名
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                // 取反，可以减少代码嵌套
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                // 包名.类名
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                // 实例化 要用到Class.forName(className);
                registryBeanClasses.add(className);
            }

        }
    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
