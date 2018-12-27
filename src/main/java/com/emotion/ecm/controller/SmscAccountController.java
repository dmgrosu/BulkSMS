package com.emotion.ecm.controller;

import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.dto.SmscAccountDto;
import com.emotion.ecm.service.AppUserService;
import com.emotion.ecm.service.SmscAccountService;
import com.emotion.ecm.validation.AjaxResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/smscAccount")
public class SmscAccountController {

    private SmscAccountService smscAccountService;
    private AppUserService userService;

    @Autowired
    public SmscAccountController(SmscAccountService smscAccountService, AppUserService userService) {
        this.smscAccountService = smscAccountService;
        this.userService = userService;
    }

    @GetMapping(value = "/list")
    public String getList(Model model) {

        AppUser currUser = userService.getAuthenticatedUser();

        model.addAttribute("smscAccounts", smscAccountService.getDtoListByAccount(currUser.getAccount()));

        return "smscAccount/list";
    }

    @PostMapping(value = "/save")
    @ResponseBody
    public AjaxResponseBody saveSmscAccount(@Valid @RequestBody SmscAccountDto smscAccountDto, BindingResult bindingResult) {

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (bindingResult.hasErrors()) {
            result.setValid(false);
            allErrors.addAll(bindingResult.getFieldErrors());
        }

        if (smscAccountService.checkDuplicate(smscAccountDto)) {
            result.setValid(false);
            allErrors.add(new FieldError("smscAccountDto", "ipAddress", "duplicate SMSC found"));
        }

        if (result.isValid()) {
            smscAccountService.save(smscAccountDto);
        }

        return result;
    }

    @PostMapping(value = "/delete")
    @ResponseBody
    public AjaxResponseBody deleteSmscAccount(@RequestBody SmscAccountDto smscAccountDto) {

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (smscAccountDto.getSmscAccountId() == 0) {
            result.setValid(false);
            allErrors.add(new FieldError("smscAccoundDto", "smscAccountId", "smscAccountId is 0"));
        }

        if (result.isValid()) {
            smscAccountService.deleteById(smscAccountDto.getSmscAccountId());
        }

        return result;
    }
}
