package com.dfl.reddog.service;


import com.dfl.reddog.spring.ApplicationContext;

public class Test {

    public static void main(String[] args) throws ClassNotFoundException {
        ApplicationContext context = new ApplicationContext(AppConfig.class);

//        UserService userService = (UserService) context.getBean("userService");
//        userService.say();
//        System.out.println(userService);
//        userService = (UserService) context.getBean("userService");
//        System.out.println(userService);

        GirlService girlService = (GirlService) context.getBean("girlService");
//        System.out.println(girlService);
//        girlService = (GirlService) context.getBean("girlService");
//        System.out.println(girlService);
//        girlService = (GirlService) context.getBean("girlService");
//        System.out.println(girlService);
//        girlService = (GirlService) context.getBean("girlService");
//        System.out.println(girlService);

        girlService.show();
    }
}
