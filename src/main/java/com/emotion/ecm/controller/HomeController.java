package com.emotion.ecm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller(value = "/home")
public class HomeController {

    @GetMapping(value = "/")
    public String homePage(Model model) {
        return "home";
    }

}
