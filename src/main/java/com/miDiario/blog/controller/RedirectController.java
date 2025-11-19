package com.miDiario.blog.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RedirectController {

    @GetMapping("/")
    public String home() {
        return "redirect:/html/login.html";
    }
}
