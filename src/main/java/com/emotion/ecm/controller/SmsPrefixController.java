package com.emotion.ecm.controller;

import com.emotion.ecm.exception.AccountException;
import com.emotion.ecm.exception.PrefixException;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.AppUser;
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
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

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

    @GetMapping(value = "/getGroupNameById")
    @ResponseBody
    public ResponseEntity<?> getGroupDtoById(@RequestParam(name = "id") int id) {
        try {
            PrefixGroupDto result = prefixService.getGroupDtoById(id);
            return ResponseEntity.ok(result);
        } catch (PrefixException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping(value = "/getPrefixById")
    @ResponseBody
    public ResponseEntity<?> getPrefixDtoById(@RequestParam(name = "id") int id) {
        try {
            PrefixDto result = prefixService.getPrefixDtoById(id);
            return ResponseEntity.ok(result);
        } catch (PrefixException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
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

        if (prefixDto.getPrefixId() == 0) {
            if (prefixService.getByGroupIdAndPrefix(prefixDto.getGroupId(), prefixDto.getPrefix()).isPresent()) {
                result.setValid(false);
                allErrors.add(new FieldError("prefixDto", "prefixId", "prefixId is 0"));
            }
        }

        if (result.isValid()) {
            try {
                prefixService.savePrefix(prefixDto);
            } catch (PrefixException e) {
                result.setValid(false);
                allErrors.add(new FieldError("prefixDto", "prefixId", e.getMessage()));
            }
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

        if (groupDto.getGroupId() == 0) {
            if (prefixService.getGroupByAccountAndName(currAccount, groupDto.getGroupName()).isPresent()) {
                result.setValid(false);
                allErrors.add(new FieldError("groupDto", "groupId", "groupId is 0"));
            }
        }

        if (result.isValid()) {
            try {
                prefixService.savePrefixGroup(groupDto, currAccount);
            } catch (AccountException e) {
                result.setValid(false);
                allErrors.add(new FieldError("groupDto", "accountId", e.getMessage()));
            }
        }

        return result;
    }

    @PostMapping(value = "/deletePrefix")
    @ResponseBody
    public AjaxResponseBody deletePrefix(@RequestBody PrefixDto prefixDto, BindingResult bindingResult) {

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

}
