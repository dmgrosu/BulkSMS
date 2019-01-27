package com.emotion.ecm.controller;

import com.emotion.ecm.model.dto.UserDto;
import com.emotion.ecm.service.AppRoleService;
import com.emotion.ecm.service.AppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private AppUserService userService;
    private AppRoleService roleService;

    @Autowired
    public UserController(AppUserService userService, AppRoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping(value = "/list")
    public String getList(Model model) {
        model.addAttribute("users", userService.getAllDto());
        return "user/list";
    }

    @PostMapping(value = "/changeStatus")
    @ResponseBody
    public ResponseEntity<?> changeStatus(@RequestBody UserDto dto) {
        try {
            userService.changeUserStatus(dto);
            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            LOGGER.warn(ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}
