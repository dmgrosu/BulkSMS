package com.emotion.ecm.controller;

import com.emotion.ecm.service.AppRoleService;
import com.emotion.ecm.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller(value = "/user")
public class UserController {

    private AppUserService userService;
    private AppRoleService roleService;

    @Autowired
    public UserController(AppUserService userService, AppRoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

}
