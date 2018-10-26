package com.emotion.ecm.controller;

import com.emotion.ecm.service.AppRoleService;
import com.emotion.ecm.service.AppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = {"/","/login","/index"})
public class LoginController {

    private final static Logger LOG = LoggerFactory.getLogger(LoginController.class);

    private AppUserService userService;
    private AppRoleService roleService;

    @Autowired
    public LoginController(AppUserService userService, AppRoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String loginForm(final Model model) {
        model.addAttribute("loginError", "error");
        return "loginForm";
    }

}
