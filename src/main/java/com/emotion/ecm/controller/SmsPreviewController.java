package com.emotion.ecm.controller;

import com.emotion.ecm.dao.SmsPreviewDao;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmsPreview;
import com.emotion.ecm.model.dto.PreviewDto;
import com.emotion.ecm.service.*;
import com.emotion.ecm.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/preview")
public class SmsPreviewController {

    private SmsPreviewService smsPreviewService;
    private AppUserService userService;
    private SmsTypeService typeService;
    private SmsPriorityService priorityService;
    private AccountService accountService;

    @Autowired
    public SmsPreviewController(SmsPreviewService smsPreviewService, AppUserService userService,
                                SmsTypeService typeService, SmsPriorityService priorityService,
                                AccountService accountService) {
        this.smsPreviewService = smsPreviewService;
        this.userService = userService;
        this.typeService = typeService;
        this.priorityService = priorityService;
        this.accountService = accountService;
    }

    @GetMapping(value = "/list")
    public String showList(Model model) {
        AppUser user = userService.getAuthenticatedUser();
        List<SmsPreview> smsPreviews = smsPreviewService.getAllByAccountAndUser(user.getAccount(), user);
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
        previewDto.setAccountId(account.getId());
        previewDto.setTps(account.getTps());

        model.addAttribute("preview", previewDto);
        model.addAttribute("types", typeService.getAll());
        model.addAttribute("priorities", priorityService.getAll());

        return "preview/form";
    }

    @PostMapping(value = "/create")
    public String savePreview(@ModelAttribute(name = "preview") PreviewDto previewDto,
                              BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("preview", previewDto);
            model.addAttribute("errorMessage", "Form error!");
            return "redirect:/create?error";
        }

        try {
            smsPreviewService.createNewPreview(previewDto);
        } catch (ParseException e) {
            bindingResult.rejectValue("errors", e.getMessage());
            return "redirect:/create?error";
        }

        return "preview/list";
    }
}
