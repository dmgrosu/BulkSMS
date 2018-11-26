package com.emotion.ecm.controller;

import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmppAddress;
import com.emotion.ecm.service.AppUserService;
import com.emotion.ecm.service.SmppAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping(value = "smppAddress")
public class SmppAddressController {

    private SmppAddressService smppAddressService;
    private AppUserService userService;

    @Autowired
    public SmppAddressController(SmppAddressService smppAddressService, AppUserService userService) {
        this.smppAddressService = smppAddressService;
        this.userService = userService;
    }

    @GetMapping(value = "/list")
    public String getList(Model model) {

        AppUser currUser = userService.getAuthenticatedUser();
        List<SmppAddress> smppAddresses = smppAddressService.getAllByAccount(currUser.getAccount());
        model.addAttribute("smppAddresses", smppAddresses);

        return "smppAddress/list";
    }

}
