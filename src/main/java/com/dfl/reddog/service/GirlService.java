package com.dfl.reddog.service;


import com.dfl.reddog.spring.Autowired;
import com.dfl.reddog.spring.Component;

//@Scope("prototype")
@Component
public class GirlService {

    @Autowired
    private UserInterFace userService;

    public void show() {
        System.out.println("girl shows awesome body and say: ");
        userService.say();
    }
}
