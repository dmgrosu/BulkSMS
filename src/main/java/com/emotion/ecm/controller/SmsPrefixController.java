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
import org.springframework.validation.FieldError;
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
    public AjaxResponseBody savePrefix(@Valid @RequestBody PrefixDto prefixDto, BindingResult bindingResult) {

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (bindingResult.hasErrors()) {
            result.setValid(false);
            allErrors.addAll(bindingResult.getFieldErrors());
        }

        try {
            if (prefixDto.getPrefixId() == 0) {
                if (checkPrefixDuplicate(prefixDto)) {
                    result.setValid(false);
                    allErrors.add(new FieldError("prefixDto", "prefixId", "prefixId is 0"));
                } else {
                    prefixService.createNewPrefix(prefixDto);
                }
            } else {
                prefixService.updatePrefix(prefixDto);
            }
        } catch (NullPointerException e) {
            result.setValid(false);
            allErrors.add(new FieldError("prefixDto", "prefix", e.getMessage()));
        }

        return result;
    }

    @PostMapping(value = "/saveGroup")
    @ResponseBody
    public AjaxResponseBody saveGroup(@Valid @RequestBody PrefixGroupDto groupDto, BindingResult bindingResult) {

        AppUser currUser = userService.getAuthenticatedUser();
        Account currAccount = currUser.getAccount();

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (bindingResult.hasErrors()) {
            result.setValid(false);
            allErrors.addAll(bindingResult.getFieldErrors());
        }

        try {
            if (groupDto.getGroupId() == 0) {
                if (checkGroupDuplicate(currAccount, groupDto)) {
                    result.setValid(false);
                    allErrors.add(new FieldError("groupDto", "groupId", "groupId is 0"));
                } else {
                    groupDto.setAccountId(currAccount.getId());
                    prefixService.createNewGroup(groupDto);
                }
            } else {
                prefixService.updateGroup(groupDto);
            }
            result.setValid(true);
        } catch (NullPointerException e) {
            result.setValid(false);
            allErrors.add(new FieldError("groupDto", "groupName", e.getMessage()));
        }

        return result;
    }

    @PostMapping(value = "/deletePrefix")
    @ResponseBody
    public AjaxResponseBody deletePrefix(@Valid @RequestBody PrefixDto prefixDto, BindingResult bindingResult) {

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (bindingResult.hasErrors()) {
            result.setValid(false);
            allErrors.addAll(bindingResult.getFieldErrors());
        }

        try {
            prefixService.deletePrefix(prefixDto.getPrefixId());
        } catch (Exception e) {
            result.setValid(false);
            allErrors.add(new FieldError("prefixDto", "prefix", e.getMessage()));
        }

        return result;
    }

    @PostMapping(value = "/deleteGroup")
    @ResponseBody
    public AjaxResponseBody deleteGroup(@RequestBody PrefixGroupDto groupDto, BindingResult bindingResult) {

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (bindingResult.hasErrors()) {
            result.setValid(false);
            allErrors.addAll(bindingResult.getFieldErrors());
        }

        try {
            prefixService.deleteGroup(groupDto.getGroupId());
        } catch (Exception e) {
            result.setValid(false);
            allErrors.add(new FieldError("prefixDto", "prefix", e.getMessage()));
        }

        return result;
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
