package com.emotion.ecm.controller;

import com.emotion.ecm.service.AccountService;
import com.emotion.ecm.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "admin")
public class AdminController {

    private AppUserService userService;
    private AccountService accountService;

    @Autowired
    public AdminController(AppUserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping(value = "/accountList")
    public String adminHome() {
        return "redirect:/account/list";
    }
}
