package com.emotion.ecm.controller;

import com.emotion.ecm.exception.SmscAccountException;
import com.emotion.ecm.model.dto.SmscAccountDto;
import com.emotion.ecm.service.SmscAccountService;
import com.emotion.ecm.validation.AjaxResponseBody;
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
@RequestMapping(value = "/smscAccount")
public class SmscAccountController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmscAccountController.class);

    private SmscAccountService smscAccountService;

    @Autowired
    public SmscAccountController(SmscAccountService smscAccountService) {
        this.smscAccountService = smscAccountService;
    }

    @GetMapping(value = "/list")
    public String getList(Model model) {

        model.addAttribute("smscAccounts", smscAccountService.getAllDto());

        return "smscAccount/list";
    }

    @GetMapping(value = "/getById")
    @ResponseBody
    public ResponseEntity<SmscAccountDto> findDtoById(@RequestParam(name = "id") int id) {
        try {
            SmscAccountDto result = smscAccountService.getDtoById(id);
            return ResponseEntity.ok(result);
        } catch (SmscAccountException e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.badRequest().body(new SmscAccountDto());
        }

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

    @GetMapping(value = "/getAllNames")
    @ResponseBody
    public List<SmscAccountDto> getAllNames() {
        return smscAccountService.getAllNames();
    }
}
