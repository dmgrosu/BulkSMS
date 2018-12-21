package com.emotion.ecm.controller;

import com.emotion.ecm.exception.AccountException;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmppAddress;
import com.emotion.ecm.model.dto.PrefixDto;
import com.emotion.ecm.model.dto.SmppAddressDto;
import com.emotion.ecm.service.AppUserService;
import com.emotion.ecm.service.SmppAddressService;
import com.emotion.ecm.validation.AjaxResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<SmppAddressDto> smppAddresses = smppAddressService.getAllDtoByAccount(currUser.getAccount());
        model.addAttribute("smppAddresses", smppAddresses);

        return "smppAddress/list";
    }

    @PostMapping(value = "/save")
    @ResponseBody
    public AjaxResponseBody saveSmppAddress(@Valid @RequestBody SmppAddressDto smppAddressDto, BindingResult bindingResult) {

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (bindingResult.hasErrors()) {
            result.setValid(false);
            allErrors.addAll(bindingResult.getFieldErrors());
        }

        AppUser currUser = userService.getAuthenticatedUser();
        Account currAccount = currUser.getAccount();

        if (currAccount == null) {
            result.setValid(false);
            allErrors.add(new FieldError("smppAddressDto", "accountId", "user account not found"));
            return result;
        }

        try {
            if (smppAddressDto.getSmppAddressId() == 0) {
                if (checkDuplicateAddress(currAccount, smppAddressDto.getAddress())) {
                    result.setValid(false);
                    allErrors.add(new FieldError("smppAddressDto", "address", "duplicate smpp address found"));
                } else {
                    smppAddressDto.setAccountId(currAccount.getId());
                    smppAddressService.saveNewSmppAddress(smppAddressDto);
                }
            } else {
                smppAddressService.updateSmppAddress(smppAddressDto);
            }
        } catch (AccountException e) {
            result.setValid(false);
            allErrors.add(new FieldError("smppAddressDto", "address", e.getMessage()));
        }

        return result;
    }

    @PostMapping(value = "/delete")
    @ResponseBody
    public AjaxResponseBody deleteSmppAddress(@RequestBody SmppAddressDto smppAddressDto, BindingResult bindingResult) {

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (bindingResult.hasErrors()) {
            result.setValid(false);
            allErrors.addAll(bindingResult.getFieldErrors());
        }

        try {
            if (smppAddressDto.getSmppAddressId() == 0) {
                result.setValid(false);
                allErrors.add(new FieldError("smppAddressDto", "smppAddressId", "smpp address id is 0"));
            } else {
                smppAddressService.deleteById(smppAddressDto.getSmppAddressId());
            }
        } catch (Exception e) {
            result.setValid(false);
            allErrors.add(new FieldError("smppAddressDto", "address", e.getMessage()));
        }

        return result;
    }


    private boolean checkDuplicateAddress(Account account, String address) {
        return smppAddressService.getByAccountAndAddress(account, address).isPresent();
    }

}
