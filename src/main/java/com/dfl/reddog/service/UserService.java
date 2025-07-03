package com.dfl.reddog.service;


import com.dfl.reddog.spring.BeanNameAware;
import com.dfl.reddog.spring.Component;
import com.dfl.reddog.spring.InitializeBean;
import com.dfl.reddog.spring.PrototypeAware;

//@Scope("prototype")
@Component("userService")
public class UserService implements
        BeanNameAware, InitializeBean, PrototypeAware,
        UserInterFace {

    private String beanName;

    private String type;

    private int id;

    public void say() {
        System.out.println("oi ! " + beanName + " is " + type + ", id : " + id);
    }

//    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

//    @Override
    public void afterPropertiesSet() {
        id = 1234;
        System.out.println(beanName + "执行了初始化方法");
    }

//    @Override
    public void setPrototype(String prototype) {
        type = prototype;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
