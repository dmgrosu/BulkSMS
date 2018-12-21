package com.emotion.ecm.controller;

import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmsPreview;
import com.emotion.ecm.model.dto.PreviewDto;
import com.emotion.ecm.service.*;
import com.emotion.ecm.util.DateUtil;
import com.emotion.ecm.validation.AjaxResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/preview")
public class SmsPreviewController {

    private SmsPreviewService smsPreviewService;
    private AppUserService userService;
    private SmsTypeService typeService;
    private SmsPriorityService priorityService;
    private AccountDataService accountDataService;
    private GroupService groupService;
    private SmppAddressService smppAddressService;
    private ExpirationTimeService expirationTimeService;

    @Autowired
    public SmsPreviewController(SmsPreviewService smsPreviewService, AppUserService userService,
                                SmsTypeService typeService, SmsPriorityService priorityService,
                                GroupService groupService, AccountDataService accountDataService,
                                SmppAddressService smppAddressService, ExpirationTimeService expirationTimeService) {
        this.smsPreviewService = smsPreviewService;
        this.userService = userService;
        this.typeService = typeService;
        this.priorityService = priorityService;
        this.groupService = groupService;
        this.accountDataService = accountDataService;
        this.smppAddressService = smppAddressService;
        this.expirationTimeService = expirationTimeService;
    }

    @GetMapping(value = "/list")
    public String showList(Model model) {
        AppUser user = userService.getAuthenticatedUser();
        List<SmsPreview> smsPreviews = smsPreviewService.getAllByUser(user);
        List<PreviewDto> previews = new ArrayList<>();
        if (!smsPreviews.isEmpty()) {
            previews = smsPreviewService.convertPreviewListToDto(smsPreviews);
        }
        model.addAttribute("previews", previews);
        return "preview/list";
    }

    @GetMapping(value = "/create")
    public String createPreviewForm(Model model) {
        AppUser currUser = userService.getAuthenticatedUser();
        Account account = currUser.getAccount();

        PreviewDto previewDto = new PreviewDto();
        previewDto.setSendDate(DateUtil.formatDate(LocalDateTime.now()));
        previewDto.setUserId(currUser.getId());
        previewDto.setTps(account.getTps());
        previewDto.setUsername(currUser.getUsername());

        addAttributes(previewDto, model, currUser, account);

        return "preview/form";
    }

    @PostMapping(value = "/create")
    public String savePreview(@Valid @ModelAttribute(name = "preview") PreviewDto previewDto,
                              BindingResult bindingResult, Model model) {

        boolean isValid = true;

        if (bindingResult.hasErrors()) {
            isValid = false;
        }

        Optional<SmsPreview> optional = smsPreviewService.getByUserIdAndName(previewDto.getUserId(), previewDto.getName());
        if (optional.isPresent()) {
            bindingResult.rejectValue("name", "name.error", "Name is not unique");
            isValid = false;
        }

        if (isValid) {
            try {
                smsPreviewService.createNewPreview(previewDto);
            } catch (ParseException e) {
                bindingResult.rejectValue("errors", e.getMessage());
                isValid = false;
            }
        }

        if (isValid) {
            return "redirect: preview/list";
        } else {
            AppUser currUser = userService.getAuthenticatedUser();
            Account account = currUser.getAccount();
            model.addAttribute("preview", previewDto);
            addAttributes(previewDto, model, currUser, account);
            return "preview/form";
        }
    }

    @PostMapping(value = "/delete")
    @ResponseBody
    public AjaxResponseBody deletePreview(@RequestBody PreviewDto previewDto) {

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        try {
            if (previewDto.getPreviewId() == 0) {
                result.setValid(false);
                allErrors.add(new FieldError("previewDto", "previewId", "preview id is 0"));
            } else {
                smsPreviewService.deleteById(previewDto.getPreviewId());
            }
        } catch (Exception e) {
            result.setValid(false);
            allErrors.add(new FieldError("previewDto", "previewId", e.getMessage()));
        }

        return result;
    }

    private void addAttributes(PreviewDto previewDto, Model model, AppUser currUser, Account account) {
        model.addAttribute("preview", previewDto);
        model.addAttribute("types", typeService.getAll());
        model.addAttribute("priorities", priorityService.getAll());
        model.addAttribute("accountDataList", accountDataService.getAllByUser(currUser));
        model.addAttribute("groupList", groupService.getAllByUser(currUser));
        model.addAttribute("originators", smppAddressService.getAllByAccount(account));
        model.addAttribute("availableExpTime", expirationTimeService.getAllByAccount(account));
    }
}
