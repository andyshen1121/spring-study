package com.study.spring.framework.support;

import com.study.spring.framework.config.GPBeanDefinition;
import com.study.spring.framework.core.GPBeanFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GPDefaultListableBeanFactory implements GPBeanFactory {

    public Map<String, GPBeanDefinition> beanDefinitionMap = new HashMap<>();

    @Override
    public Object getBean(Class beanClass) {
        return null;
    }

    @Override
    public Object getBean(String beanName) {
        return null;
    }

    public void doRegistBeanDefinition(List<GPBeanDefinition> beanDefinitions) {

    }
}
