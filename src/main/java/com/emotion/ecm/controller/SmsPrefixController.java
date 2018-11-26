package com.emotion.ecm.controller;

import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmsPrefix;
import com.emotion.ecm.model.SmsPrefixGroup;
import com.emotion.ecm.model.dto.PrefixDto;
import com.emotion.ecm.model.dto.PrefixGroupDto;
import com.emotion.ecm.service.AppUserService;
import com.emotion.ecm.service.SmsPrefixService;
import com.emotion.ecm.validation.AjaxResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/prefix")
public class SmsPrefixController {

    private SmsPrefixService prefixService;
    private AppUserService userService;

    @Autowired
    public SmsPrefixController(SmsPrefixService prefixService, AppUserService userService) {
        this.prefixService = prefixService;
        this.userService = userService;
    }

    @GetMapping(value = "/list")
    public String displayList(Model model) {
        AppUser user = userService.getAuthenticatedUser();
        List<PrefixGroupDto> groups = prefixService.getGroupDtoList(user.getAccount());
        model.addAttribute("groups", groups);
        return "prefix/list";
    }

    @PostMapping(value = "/savePrefix")
    @ResponseBody
    public ResponseEntity<?> savePrefix(@Valid @RequestBody PrefixDto prefixDto, Errors errors) {

        AjaxResponseBody result = new AjaxResponseBody();

        if (errors.hasErrors()) {
            result.setMessage("fail");
        }

        try {
            if (prefixDto.getPrefixId() == 0) {
                if (checkPrefixDuplicate(prefixDto)) {
                    errors.rejectValue("prefix", "duplicate found");
                    result.setMessage("fail");
                } else {
                    prefixService.createNewPrefix(prefixDto);
                }
            } else {
                prefixService.updatePrefix(prefixDto);
            }
            result.setMessage("success");
        } catch (NullPointerException e) {
            errors.rejectValue("global", e.getMessage());
            result.setMessage("fail");
        }

        result.setErrors(errors.getAllErrors());
        if (result.getMessage().equals("fail")) {
            return ResponseEntity.badRequest().body(result);
        } else {
            return ResponseEntity.ok(result);
        }

    }

    @PostMapping(value = "/saveGroup")
    @ResponseBody
    public ResponseEntity<?> saveGroup(@Valid @RequestBody PrefixGroupDto groupDto, Errors errors) {

        AppUser currUser = userService.getAuthenticatedUser();
        Account currAccount = currUser.getAccount();

        AjaxResponseBody result = new AjaxResponseBody();

        if (errors.hasErrors()) {
            result.setMessage("fail");
        }

        try {
            if (groupDto.getGroupId() == 0) {
                if (checkGroupDuplicate(currAccount, groupDto)) {
                    errors.rejectValue("groupName", "name duplicate");
                    result.setMessage("fail");
                } else {
                    groupDto.setAccountId(currAccount.getId());
                    prefixService.createNewGroup(groupDto);
                }
            } else {

                prefixService.updateGroup(groupDto);
            }
            result.setMessage("success");
        } catch (NullPointerException e) {
            result.setMessage("fail");
        }

        result.setErrors(errors.getAllErrors());
        if (result.getMessage().equals("fail")) {
            return ResponseEntity.badRequest().body(result);
        } else {
            return ResponseEntity.ok(result);
        }

    }

    @PostMapping(value = "/deletePrefix")
    @ResponseBody
    public ResponseEntity<?> deletePrefix(@RequestBody PrefixDto prefixDto, Errors errors) {

        AjaxResponseBody response = new AjaxResponseBody();

        if (errors.hasErrors()) {
            response.setMessage("fail");
        }

        try {
            response.setMessage("success");
            prefixService.deletePrefix(prefixDto.getPrefixId());
        } catch (Exception e) {
            response.setMessage("fail");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/deleteGroup")
    @ResponseBody
    public ResponseEntity<?> deleteGroup(@RequestBody PrefixGroupDto groupDto, Errors errors) {

        AjaxResponseBody response = new AjaxResponseBody();

        if (errors.hasErrors()) {
            response.setMessage("fail");
        }

        try {
            response.setMessage("success");
            prefixService.deleteGroup(groupDto.getGroupId());
        } catch (Exception e) {
            response.setMessage("fail");
        }

        return ResponseEntity.ok(response);
    }

    private boolean checkPrefixDuplicate(PrefixDto prefixDto) {
        Optional<SmsPrefixGroup> optionalGroup = prefixService.getGroupById(prefixDto.getGroupId());
        return optionalGroup.filter(smsPrefixGroup ->
                prefixService.getByGroupAndPrefix(smsPrefixGroup, prefixDto.getPrefix())
                        .isPresent()).isPresent();
    }

    private boolean checkGroupDuplicate(Account currAccount, PrefixGroupDto groupDto) {
        return prefixService.getGroupByAccountAndName(currAccount, groupDto.getGroupName()).isPresent();
    }

}