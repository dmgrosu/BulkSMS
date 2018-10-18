package com.emotion.ecm.controller;

import com.emotion.ecm.service.AppRoleService;
import com.emotion.ecm.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    private AppUserService userService;
    private AppRoleService roleService;

    @Autowired
    public LoginController(AppUserService userService, AppRoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping(value = "/login")
    public String loginForm(final Model model) {
        model.addAttribute("loginError", "error");
        return "login";
    }

}
