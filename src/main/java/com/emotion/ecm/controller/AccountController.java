package com.emotion.ecm.controller;

import com.emotion.ecm.exception.AccountException;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.dto.AccountDto;
import com.emotion.ecm.service.AccountService;
import com.emotion.ecm.validation.AjaxResponseBody;
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
@RequestMapping(value = "/account")
public class AccountController {

    private AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping(value = "/list")
    public String showList(Model model) {

        model.addAttribute("accounts", accountService.getAllDto());
        return "account/list";
    }

    @GetMapping(value = "/getById")
    @ResponseBody
    public ResponseEntity<Account> getById(@RequestParam(name = "accountId") int id) {
        try {
            Account account = accountService.getById(id);
            return ResponseEntity.ok(account);
        } catch (AccountException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/save")
    @ResponseBody
    public AjaxResponseBody save(@Valid @RequestBody AccountDto dto, BindingResult bindingResult) {

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (bindingResult.hasErrors()) {
            result.setValid(false);
            allErrors.addAll(bindingResult.getFieldErrors());
        }

        if (dto.getAccountId() == 0) {
            if (accountService.findByName(dto.getName()).isPresent()) {
                result.setValid(false);
                allErrors.add(new FieldError("accountDto", "name", "name duplicate found"));
            }
        }

        if (result.isValid()) {
            accountService.save(dto);
        }

        return result;
    }

    @PostMapping(value = "/delete")
    @ResponseBody
    public ResponseEntity<?> delete(@RequestBody AccountDto dto) {
        try {
            accountService.deleteById(dto.getAccountId());
            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
