package com.emotion.ecm.controller;

import com.emotion.ecm.enums.RoleName;
import com.emotion.ecm.exception.AuthorisationException;
import com.emotion.ecm.exception.ReportException;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.AppRole;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.dto.AccountDto;
import com.emotion.ecm.model.dto.report.ReportGeneral;
import com.emotion.ecm.model.dto.report.ReportRequest;
import com.emotion.ecm.model.dto.UserDto;
import com.emotion.ecm.service.AccountService;
import com.emotion.ecm.service.AppUserService;
import com.emotion.ecm.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@Controller
@RequestMapping(value = "/report")
public class ReportController {

    private ReportService reportService;
    private AppUserService userService;
    private AccountService accountService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    public ReportController(ReportService reportService, AppUserService userService,
                            AccountService accountService) {
        this.reportService = reportService;
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping(value = "/general")
    public String generalReport(Model model) {
        try {
            model.addAllAttributes(getRaportAttributes());
        } catch (AuthorisationException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "error";
        }
        return "report/general";
    }

    @GetMapping(value = "/userList")
    @ResponseBody
    public ResponseEntity<?> getUserList(@RequestParam(name = "accountId") Integer accountId) {
        if (accountId != null) {
            return ResponseEntity.ok(userService.getAllDtoByAccountId(accountId));
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/preview")
    public String previewReport(Model model) {
        try {
            model.addAllAttributes(getRaportAttributes());
        } catch (AuthorisationException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "error";
        }
        return "report/preview";
    }

    @GetMapping(value = "/msisdn")
    public String msisdnReport(Model model) {
        return "report/msisdn";
    }

    @PostMapping(value = "/general")
    @ResponseBody
    public ResponseEntity<?> generateGeneralReport(@Valid @RequestBody ReportRequest reportRequest,
                                                   BindingResult bindingResult) {

        if (reportRequest.getDetails().isEmpty()) {
            bindingResult.rejectValue("details", "details.error", "details must be selected");
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.ok(bindingResult.getFieldErrors());
        }
        if (reportRequest.getEndDate().isBefore(reportRequest.getStartDate())) {
            bindingResult.rejectValue("endDate", "endDate.error", "must be after startDate");
            return ResponseEntity.ok(bindingResult.getFieldErrors());
        }
        try {
            ReportGeneral report = reportService.generateGeneralReport(reportRequest);
            return ResponseEntity.ok(report);
        } catch (ReportException ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    private Map<String, Object> getRaportAttributes() throws AuthorisationException {
        Map<String, Object> result = new HashMap<>();
        AppUser user = userService.getAuthenticatedUser();
        Set<AppRole> userRoles = user.getRoles();
        RoleName currentRole = null;
        for (AppRole userRole : userRoles) {
            if (userRole.getName() == RoleName.ADMIN) {
                currentRole = RoleName.ADMIN;
            } else if (userRole.getName() == RoleName.REPORT_USER) {
                currentRole = RoleName.REPORT_USER;
            }
        }
        if (currentRole == null) {
            LOGGER.warn(String.format("%s: authorisation error", user.getUsername()));
            throw new AuthorisationException("Authorisation error");
        }
        List<UserDto> users = new ArrayList<>();
        List<AccountDto> accounts = new ArrayList<>();
        if (currentRole == RoleName.REPORT_USER) {
            Account userAccount = user.getAccount();
            try {
                AccountDto dto = new AccountDto();
                dto.setAccountId(userAccount.getId());
                dto.setName(userAccount.getName());
                accounts.add(dto);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
            users.addAll(userService.getAllDtoByAccountId(userAccount.getId()));
        } else {
            accounts.addAll(accountService.getAllDto());
        }
        result.put("role", currentRole.name());
        result.put("users", users);
        result.put("accounts", accounts);
        result.put("reportRequest", new ReportRequest());
        return result;
    }
}
