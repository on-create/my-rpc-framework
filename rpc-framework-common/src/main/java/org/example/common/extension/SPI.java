package org.example.common.extension;

import java.lang.annotation.*;

/**
 * RetentionPolicy:
 *  source: 注解只保留在源文件，当Java文件编译成class文件的时候，注解被遗弃（被编译器忽略）    .java文件
 *  class: 注解被保留到class文件，但jvm加载class文件时候被遗弃，这是默认的生命周期           .class文件
 *  runtime: 注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在                   内存中的字节码
 *
 * Target:
 *  Type: 用于描述类、接口（包括注解类型）或enum声明
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
