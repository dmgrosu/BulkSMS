package com.emotion.ecm.controller;

import com.emotion.ecm.exception.AccountException;
import com.emotion.ecm.exception.BlackListException;
import com.emotion.ecm.model.dto.BlackListDto;
import com.emotion.ecm.model.dto.BlackListMsisdnDto;
import com.emotion.ecm.service.BlackListService;
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
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/blackList")
public class BlacklistController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlacklistController.class);

    private BlackListService blackListService;

    @Autowired
    public BlacklistController(BlackListService blackListService) {
        this.blackListService = blackListService;
    }

    @GetMapping(value = "/list")
    public String showList(Model model) {

        List<BlackListDto> allDto = blackListService.getAllDto();

        model.addAttribute("blackLists", allDto);

        return "blackList/list";
    }

    @PostMapping(value = "/save")
    @ResponseBody
    public AjaxResponseBody save(@Valid @RequestBody BlackListDto dto, BindingResult bindingResult) {

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (bindingResult.hasErrors()) {
            result.setValid(false);
            allErrors.addAll(bindingResult.getFieldErrors());
        }

        if (dto.getBlackListId() == 0) {
            if (blackListService.findByName(dto.getName()).isPresent()) {
                result.setValid(false);
                allErrors.add(new FieldError("blackListDto", "name", "name duplicate found"));
            }
        }

        if (result.isValid()) {
            try {
                blackListService.saveBlackList(dto);
            } catch (AccountException ex) {
                result.setValid(false);
                allErrors.add(new FieldError("blackListDto", "accountName", ex.getMessage()));
            }
        }

        return result;
    }

    @GetMapping(value = "/getById")
    @ResponseBody
    public ResponseEntity<BlackListDto> findDtoById(@RequestParam(name = "id") int id) {
        try {
            BlackListDto result = blackListService.getDtoById(id);
            return ResponseEntity.ok(result);
        } catch (BlackListException ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/getNumbersById")
    @ResponseBody
    public ResponseEntity<List<String>> getNumbersById(@RequestParam(name = "id") int id) {
        try {
            List<String> result = blackListService.getAllMsisdnByBlackListId(id);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/delete")
    @ResponseBody
    public ResponseEntity<?> deletePreview(@RequestBody int[] blackListIds) {
        try {
            if (blackListIds == null) {
                return ResponseEntity.badRequest().build();
            } else {
                for (int blackListId : blackListIds) {
                    blackListService.deleteBlackListById(blackListId);
                }
                return ResponseEntity.ok(blackListIds);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/addNumbersById")
    @ResponseBody
    public AjaxResponseBody addNumbersToBlackList(@RequestBody BlackListMsisdnDto dto, BindingResult bindingResult) {

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (bindingResult.hasErrors()) {
            result.setValid(false);
            allErrors.addAll(bindingResult.getFieldErrors());
        }

        if (result.isValid()) {
            try {
                blackListService.saveMsisdnList(dto);
            } catch (BlackListException ex) {
                result.setValid(false);
                allErrors.add(new FieldError("blackListMsisdnDto", "blackListId", ex.getMessage()));
            }
        }

        return result;
    }

}
