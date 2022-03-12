package com.study.spring.framework.context;

import com.study.spring.framework.GPBeanWrapper;
import com.study.spring.framework.annotation.GPAutowired;
import com.study.spring.framework.annotation.GPController;
import com.study.spring.framework.annotation.GPService;
import com.study.spring.framework.config.GPBeanDefinition;
import com.study.spring.framework.core.GPBeanFactory;
import com.study.spring.framework.support.GPBeanDefinitionReader;
import com.study.spring.framework.support.GPDefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GPApplicationContext implements GPBeanFactory {

    private GPDefaultListableBeanFactory registry = new GPDefaultListableBeanFactory();

    private GPBeanDefinitionReader reader;

    // 三级缓存（终极缓存）
    private Map<String, GPBeanWrapper> factoryBeanInstanceCache = new HashMap<>();

    //
    private Map<String, Object> factoryBeanObjectCache = new HashMap<>();

    public GPApplicationContext(String ... configLocations) {
        // 1. 加载配置文件
        reader = new GPBeanDefinitionReader(configLocations);

        try {
        // 2. 解析配置文件，将所有的配置信息封装成BeanDefinition对象
        List<GPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        // 3. 所有的配置信息缓存起来
            this.registry.doRegistBeanDefinition(beanDefinitions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 4. 加载非延时加载的所有的Bean
        doLoadInstance();
    }

    private void doLoadInstance() {
        // 循环调用getBean()方法
        for (Map.Entry<String, GPBeanDefinition> entry : this.registry.beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            if (!entry.getValue().isLazyInit()) {
                getBean(beanName);
            }

        }
    }

    @Override
    public Object getBean(Class beanClass) {
        return getBean(beanClass.getName());
    }

    // 从IoC容器中获得一个Bean对象
    @Override
    public Object getBean(String beanName) {
        // 1. 先拿到BeanDefinition配置信息
        GPBeanDefinition beanDefinition = registry.beanDefinitionMap.get(beanName);

        // 2. 反射实例化对象
        Object instance = instantiateBean(beanName, beanDefinition);


        // 3. 将返回的Bean的对象封装成BeanWrappper

        GPBeanWrapper beanWrapper = new GPBeanWrapper(instance);

        // 4. 执行依赖注入
        populateBean(beanName, beanDefinition, beanWrapper);

        // 5. 保存到IoC容器中
        this.factoryBeanInstanceCache.put(beanName, beanWrapper);

        return beanWrapper.getWrappedInstance();
    }

    private void populateBean(String beanName, GPBeanDefinition beanDefinition, GPBeanWrapper beanWrapper) {
        Object instance = beanWrapper.getWrappedInstance();

        Class<?> clazz = beanWrapper.getWrappedClass();

        if (!(clazz.isAnnotationPresent(GPController.class) || clazz.isAnnotationPresent(GPService.class))) {
            return;
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(GPAutowired.class)) {
                continue;
            }
            GPAutowired autowired = field.getAnnotation(GPAutowired.class);
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }
            field.setAccessible(true);

            try {
                if (this.factoryBeanInstanceCache.get(autowiredBeanName) == null) {
                    continue;
                }
                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Object instantiateBean(String beanName, GPBeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        try {
            Class<?> clazz = Class.forName(className);

            instance = clazz.newInstance();

            // 如果是代理对象，触发AOP逻辑



            this.factoryBeanObjectCache.put(beanName, instance);

        } catch (Exception e) {
            e.printStackTrace();

        }
        return instance;

    }

    public int getBeanDefinitionCount() {
        return this.registry.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return this.registry.beanDefinitionMap.keySet().toArray(new String[0]);
    }
}
