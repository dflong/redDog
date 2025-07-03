package com.dfl.reddog.spring;

public interface BeanPostProcessor {

    public Object postProcessorBeforeInitialization(Object bean, String beanName);

    public Object postProcessorAfterInitialization(Object bean, String beanName);
}
