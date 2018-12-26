package com.emotion.ecm.controller;

import com.emotion.ecm.model.AccountData;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.dto.AccountDataDto;
import com.emotion.ecm.service.AccountDataService;
import com.emotion.ecm.service.AppUserService;
import com.emotion.ecm.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping(value = "/accountData")
public class AccountDataController {

    private AccountDataService accountDataService;
    private AppUserService userService;
    private StorageService storageService;

    @Autowired
    public AccountDataController(AccountDataService accountDataService, AppUserService userService,
                                 StorageService storageService) {
        this.accountDataService = accountDataService;
        this.userService = userService;
        this.storageService = storageService;
    }

    @GetMapping(value = "/list")
    public String showList(Model model) {
        AppUser user = userService.getAuthenticatedUser();
        List<AccountData> accountDataList = accountDataService.getAllByUser(user);
        model.addAttribute("accountDataList", accountDataList);
        model.addAttribute("accountDataDto", new AccountDataDto());
        return "accountData/list";
    }

    @PostMapping(value = "/create")
    public String handleFileUpload(@ModelAttribute(name = "accountDataDto") AccountDataDto accountDataDto,
                                   BindingResult bindingResult, Model model) {

        String resultString = "success";

        if (bindingResult.hasErrors()) {
            resultString = "error";
        }

        AppUser user = userService.getAuthenticatedUser();
        Map<String, Integer> storageResult = new HashMap<>();
        try {
            String fileName = accountDataDto.getFile().getOriginalFilename();
            Optional<AccountData> optionalAccountData = accountDataService.
                    getByNameAndFileNameAndUser(accountDataDto.getName(), fileName, user);
            if (optionalAccountData.isPresent()) {
                if (!accountDataDto.isOverride()) {
                    model.addAttribute("accountDataDto", accountDataDto);
                    bindingResult.rejectValue("name", "name.error", "name is not unique");
                    return "redirect:/accountData/list?error";
                }
            }
            Path directory = accountDataService.getAccountPath(user);
            Path fullPath = directory.resolve(fileName);
            storageResult = storageService.storeAccountData(fullPath, accountDataDto.getFile());
            if (storageResult.get("valid") > 0) {
                accountDataService.saveNewAccountData(accountDataDto, user);
            }

        } catch (IOException e) {
            resultString = "error";
        }

        model.addAttribute("validDataCount", storageResult.get("valid"));
        model.addAttribute("invalidDataCount", storageResult.get("invalid"));

        List<AccountData> accountDataList = accountDataService.getAllByUser(user);
        model.addAttribute("accountDataList", accountDataList);
        model.addAttribute("accountDataDto", accountDataDto);

        if (resultString.equals("error")) {
            model.addAttribute("error", true);
        }

        return "redirect:/accountData/list?" + resultString;
    }
}
