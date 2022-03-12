package com.study.spring.framework;

import java.util.Objects;

public class GPBeanWrapper {

    private Object wrapperInstance;

    private Class<?> wrappedClass;

    public GPBeanWrapper(Object instance) {
        this.wrapperInstance = instance;
        this.wrappedClass = instance.getClass();
    }

    public Object getWrappedInstance() {
        return this.wrapperInstance;
    }

    public Class<?> getWrappedClass() {
        return this.wrappedClass;
    }
}
