package com.study.spring.test.controller;

import com.study.spring.framework.annotation.GPController;
import com.study.spring.framework.annotation.GPRequestMapping;
import com.study.spring.framework.annotation.GPRequestParam;

@GPController
@GPRequestMapping("/test")
public class TestController {

    @GPRequestMapping("/A")
    public String testA() {
        return "testA";
    }

    @GPRequestMapping("/B")
    public String testB() {
        return "testB";
    }

    @GPRequestMapping("/C")
    public String testC(@GPRequestParam("a") String a, @GPRequestParam("b") String b) {
        return "参数1 = " + a + "\n" + "参数2 = " + b;
    }
}
