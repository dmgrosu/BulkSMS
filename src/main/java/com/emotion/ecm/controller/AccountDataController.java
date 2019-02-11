package com.emotion.ecm.controller;

import com.emotion.ecm.model.AccountData;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.dto.AccountDataDto;
import com.emotion.ecm.service.AccountDataService;
import com.emotion.ecm.service.AppUserService;
import com.emotion.ecm.service.StorageService;
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
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Controller
@RequestMapping(value = "/accountData")
public class AccountDataController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDataController.class);

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
    @ResponseBody
    public ResponseEntity<?> handleFileUpload(@Valid @ModelAttribute AccountDataDto accountDataDto,
                                             BindingResult bindingResult) {

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (bindingResult.hasErrors()) {
            result.setValid(false);
            allErrors.addAll(bindingResult.getFieldErrors());
        }

        AppUser user = userService.getAuthenticatedUser();
        String fileName = accountDataDto.getFile().getOriginalFilename();
        Optional<AccountData> optionalAccountData = accountDataService.
                getByNameAndFileNameAndUser(accountDataDto.getName(), fileName, user);
        if (optionalAccountData.isPresent()) {
            if (!accountDataDto.isOverride()) {
                result.setValid(false);
                allErrors.add(new FieldError("accountDataDto", "name", "name is not unique"));
            }
        }

        Map<String, Integer> storageResult;
        if (result.isValid()) {
            try {
                Path directory = accountDataService.getAccountPath(user);
                Path fullPath = directory.resolve(fileName);
                storageResult = storageService.storeAccountData(fullPath, accountDataDto.getFile());
                if (storageResult.get("valid") > 0) {
                    accountDataService.saveNewAccountData(accountDataDto, user);
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.ok(storageResult);
    }

    @GetMapping(value = "/getFileContent")
    @ResponseBody
    public ResponseEntity<?> getFileContent(@RequestParam(name = "id") int accountDataId) {
        try {
            List<String> numbersList = accountDataService.getNumbersFromFile(accountDataId);
            return ResponseEntity.ok(numbersList);
        } catch (IOException e1) {
            return ResponseEntity.notFound().build();
        } catch (Exception e2) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/deleteById")
    @ResponseBody
    public ResponseEntity<?> delete(@RequestBody AccountDataDto dto) {
        try {
            accountDataService.deleteById(dto.getAccountDataId());
            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

}
