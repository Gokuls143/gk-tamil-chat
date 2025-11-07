package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/landing")
    public String landing() {
        return "landing"; // resolves to src/main/resources/templates/landing.html (Thymeleaf)
    }
}