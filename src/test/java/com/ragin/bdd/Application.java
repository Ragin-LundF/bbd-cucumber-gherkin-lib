package com.ragin.bdd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.ragin.bdd.cucumbertests.library")
public class Application {
    public static void main (String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
    }
}
