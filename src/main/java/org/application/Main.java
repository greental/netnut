package org.application;

import org.loadbalancer.LoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.loadbalancer")
public class Main {

    @Autowired
    LoadBalancer loadBalancer;
    public static void main(String[] args) {
        SpringApplication.run(LoadBalancer.class, args);
    }
}