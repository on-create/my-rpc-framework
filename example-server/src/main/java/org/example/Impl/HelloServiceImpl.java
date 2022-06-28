package org.example.Impl;

import lombok.extern.slf4j.Slf4j;
import org.example.Hello;
import org.example.HelloService;

@Slf4j
public class HelloServiceImpl implements HelloService {

    static {
        System.out.println("HelloServiceImpl被创建了");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl返回: {}.", result);
        return result;
    }
}
