package com.dfl.reddog.spring;

public class BeanDefinition {

    private Class type;

    private String scope; // 单例、多例


















    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
