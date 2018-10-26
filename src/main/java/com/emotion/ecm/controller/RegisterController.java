package com.emotion.ecm.controller;

import com.emotion.ecm.model.dto.UserDto;
import com.emotion.ecm.service.AppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping(value = "/register")
public class RegisterController {

    private AppUserService userService;

    @Autowired
    public RegisterController(AppUserService userService) {
        this.userService = userService;
    }

    private final static Logger LOG = LoggerFactory.getLogger(RegisterController.class);

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @PostMapping
    public String register(@ModelAttribute("user") @Valid UserDto userDto,
                           BindingResult result, Model model) {

        model.addAttribute("user", userDto);

        if (userService.getByUsername(userDto.getUsername()).isPresent()) {
            result.rejectValue("username", "username.error", "Username is not unique!");
        }

        if (userService.getByEmail(userDto.getEmail()).isPresent()) {
            result.rejectValue("email", "email.error", "E-mail is not unique!");
        }

        if (result.hasErrors()) {
            return "redirect:/register?error";
        }

        userService.registerNewUser(userDto);

        return "redirect:/register?success";
    }

}
