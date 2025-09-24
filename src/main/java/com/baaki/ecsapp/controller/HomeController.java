package com.baaki.ecsapp.controller;

import com.baaki.ecsapp.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private PhotoService photoService;

    @GetMapping("/")
    public String index(Model model) {
        try {
            long photoCount = photoService.getPhotoCount();
            model.addAttribute("photoCount", photoCount);
        } catch (Exception e) {
            model.addAttribute("photoCount", 0);
        }

        model.addAttribute("fullName", "Abdul Baaki N-Nyeyam Hudu");
        model.addAttribute("labName", "ECS Photo Gallery Lab");
        model.addAttribute("lastUpdated", "New Wednesday");
        return "index";
    }
}