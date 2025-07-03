package com.dfl.reddog.service;

import com.dfl.reddog.spring.BeanPostProcessor;
import com.dfl.reddog.spring.Component;

import java.lang.reflect.Proxy;

@Component
public class DflBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessorBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessorAfterInitialization(Object bean, String beanName) {
        if (beanName.equals("userService")) {
            Object proxyInstance = Proxy.newProxyInstance(this.getClass().getClassLoader(), bean.getClass().getInterfaces(), (proxy, method, args) -> {
                System.out.println("aop 执行前");
                Object invoke = method.invoke(bean, args);
                System.out.println("aop 执行后");
                return invoke;
            });
            return proxyInstance;
        }
        return bean;
    }
}
