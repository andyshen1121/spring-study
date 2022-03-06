package com.study.spring.framework.context;

import com.study.spring.framework.config.GPBeanDefinition;
import com.study.spring.framework.core.GPBeanFactory;
import com.study.spring.framework.support.GPBeanDefinitionReader;
import com.study.spring.framework.support.GPDefaultListableBeanFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GPApplicationContext implements GPBeanFactory {



    private GPDefaultListableBeanFactory registry = new GPDefaultListableBeanFactory();

    private GPBeanDefinitionReader reader;

    public GPApplicationContext(String ... configLocations) {
        // 1. 加载配置文件
        reader = new GPBeanDefinitionReader(configLocations);

        // 2. 解析配置文件，将所有的配置信息封装成BeanDefinition对象
        List<GPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        // 3. 所有的配置信息缓存起来
        this.registry.doRegistBeanDefinition(beanDefinitions);

        // 4. 加载非延时加载的所有的Bean
        doLoadInstance();
    }

    private void doLoadInstance() {
        // 循环调用getBean()方法
        for (Map.Entry<String, GPBeanDefinition> entry : this.registry.beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            getBean(beanName);
        }
    }

    @Override
    public Object getBean(Class beanClass) {
        return null;
    }

    @Override
    public Object getBean(String beanName) {
        return null;
    }
}
