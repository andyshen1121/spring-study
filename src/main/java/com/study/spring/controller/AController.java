package com.study.spring.controller;

import com.study.spring.annotation.GPController;
import com.study.spring.annotation.GPRequestMapping;

@GPController
@GPRequestMapping("/test")
public class AController {

    @GPRequestMapping("/A")
    public String testA() {
        return "testA";
    }

    @GPRequestMapping("/B")
    public String testB() {
        return "testB";
    }
}
