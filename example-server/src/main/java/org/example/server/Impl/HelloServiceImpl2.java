package org.example.server.Impl;

import lombok.extern.slf4j.Slf4j;
import org.example.api.Hello;
import org.example.api.HelloService;

@Slf4j
public class HelloServiceImpl2 implements HelloService {

    static {
        System.out.println("HelloServiceImpl--2被创建了.");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl--2收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl--2返回: {}.", result);
        return result;
    }
}
