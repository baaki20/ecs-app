package com.baaki.ecsapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public String index() {
        return "Hello from Abdul Baaki N-Nyeyam Hudu! This is the ECS CI/CD Lab. Wednesday";
    }
}
