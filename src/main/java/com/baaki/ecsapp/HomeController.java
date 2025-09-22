package com.baaki.ecsapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("fullName", "Abdul Baaki N-Nyeyam Hudu");
        model.addAttribute("labName", "ECS CI/CD Lab");
        model.addAttribute("lastUpdated", "New Today");
        return "index";
    }
}