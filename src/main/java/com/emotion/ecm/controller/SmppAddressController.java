package com.emotion.ecm.controller;

import com.emotion.ecm.exception.AccountException;
import com.emotion.ecm.exception.SmppAddressException;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.dto.SmppAddressDto;
import com.emotion.ecm.service.AppUserService;
import com.emotion.ecm.service.SmppAddressService;
import com.emotion.ecm.validation.AjaxResponseBody;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "smppAddress")
public class SmppAddressController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmppAddressController.class);

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
        try {
            model.addAttribute("smppAddresses", smppAddressService.getAllDtoByAccount(currUser.getAccount()));
            model.addAttribute("allTonValues", TypeOfNumber.values());
            model.addAttribute("allNpiValues", NumberingPlanIndicator.values());
        } catch (AccountException e) {
            LOGGER.error(e.getMessage());
        }
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

        if (smppAddressDto.getSmppAddressId() == 0) {
            if (smppAddressService.getByAccountAndAddress(currAccount, smppAddressDto.getAddress()).isPresent()) {
                result.setValid(false);
                allErrors.add(new FieldError("smppAddressDto", "address", "duplicate smpp address found"));
            }
        }

        if (result.isValid()) {
            try {
                smppAddressService.saveSmppAddress(smppAddressDto, currAccount);
            } catch (Exception e) {
                result.setValid(false);
                allErrors.add(new FieldError("smppAddressDto", "address", e.getMessage()));
                LOGGER.error(e.getMessage());
            }
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
            LOGGER.error(e.getMessage());
        }

        return result;
    }

    @GetMapping(value = "/getById")
    @ResponseBody
    public ResponseEntity<SmppAddressDto> findDtoById(@RequestParam(name = "id") int id) {
        try {
            SmppAddressDto result = smppAddressService.getDtoById(id);
            return ResponseEntity.ok(result);
        } catch (SmppAddressException ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}
