package com.emotion.ecm.controller;

import com.emotion.ecm.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/report")
public class ReportController {

    private ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping(value = "/general")
    public String generalReport(Model model) {
        return "report/general";
    }

    @GetMapping(value = "/preview")
    public String previewReport(Model model) {
        return "report/preview";
    }

    @GetMapping(value = "/msisdn")
    public String msisdnReport(Model model) {
        return "report/msisdn";
    }
}
