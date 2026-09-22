package com.example.belajar_spring_docker_v2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Halo! Spring Boot saya berhasil berjalan 🚀";
    }
}