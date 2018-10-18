package com.emotion.ecm.controller;

import com.emotion.ecm.model.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller(value = "/register")
public class RegisterController {

    private final static Logger LOG = LoggerFactory.getLogger(RegisterController.class);

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @PostMapping
    public String register(Model model) {

        return "register?success";
    }

}
