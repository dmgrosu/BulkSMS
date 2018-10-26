package com.emotion.ecm.controller;

import com.emotion.ecm.dao.SmsPreviewDao;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmsPreview;
import com.emotion.ecm.model.dto.PreviewDto;
import com.emotion.ecm.service.AppUserService;
import com.emotion.ecm.service.SmsPreviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/preview")
public class SmsPreviewController {

    private SmsPreviewService smsPreviewService;
    private AppUserService userService;

    @Autowired
    public SmsPreviewController(SmsPreviewService smsPreviewService, AppUserService userService) {
        this.smsPreviewService = smsPreviewService;
        this.userService = userService;
    }

    @GetMapping(value = "/list")
    public String showList(Model model) {
        AppUser user = userService.getAuthenticatedUser();
        List<SmsPreview> smsPreviews = smsPreviewService.getAllByAccountAndUser(user.getAccount(), user);
        List<PreviewDto> previews = new ArrayList<>();
        if (smsPreviews.isEmpty()) {
            previews = smsPreviewService.convertPreviewListToDto(smsPreviews);
        }
        model.addAttribute("previews", previews);
        return "preview/list";
    }
}
