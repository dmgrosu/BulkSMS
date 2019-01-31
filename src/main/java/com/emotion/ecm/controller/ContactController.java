package com.emotion.ecm.controller;

import com.emotion.ecm.exception.ContactException;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.dto.ContactDto;
import com.emotion.ecm.model.dto.ContactGroupDto;
import com.emotion.ecm.service.AppUserService;
import com.emotion.ecm.service.ContactService;
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
@RequestMapping(value = "/contact")
public class ContactController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactController.class);

    private AppUserService userService;
    private ContactService contactService;

    @Autowired
    public ContactController(AppUserService userService, ContactService contactService) {
        this.userService = userService;
        this.contactService = contactService;
    }

    @GetMapping(value = "/groupList")
    public String groupList(Model model) {
        AppUser user = userService.getAuthenticatedUser();
        model.addAttribute("groups", contactService.getAllGroupDtoByUserId(user.getId(), true));
        return "contact/groupList";
    }

    @GetMapping(value = "/contactList")
    public String contactList(Model model) {
        AppUser user = userService.getAuthenticatedUser();
        model.addAttribute("contacts", contactService.getAllContactDtoByUserId(user));
        return "contact/contactList";
    }

    @GetMapping(value = "/getGroupById")
    public ResponseEntity<ContactGroupDto> getGroupById(@RequestParam(name = "id") int id) {
        try {
            return ResponseEntity.ok(contactService.getGroupDtoById(id));
        } catch (ContactException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/getAllGroupNames")
    public ResponseEntity<List<ContactGroupDto>> getAllGroupNames() {
        AppUser currUser = userService.getAuthenticatedUser();
        try {
            return ResponseEntity.ok(contactService.getAllGroupDtoByUserId(currUser.getId(), false));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/getContactById")
    public ResponseEntity<ContactDto> getContactById(@RequestParam(name = "id") int id) {
        try {
            return ResponseEntity.ok(contactService.getContactDtoById(id));
        } catch (ContactException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/saveGroup")
    @ResponseBody
    public AjaxResponseBody saveGroup(@Valid @RequestBody ContactGroupDto dto, BindingResult bindingResult) {

        AppUser currUser = userService.getAuthenticatedUser();

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (bindingResult.hasErrors()) {
            result.setValid(false);
            allErrors.addAll(bindingResult.getFieldErrors());
        }

        if (dto.getGroupId() == 0) {
            if (contactService.findGroupByNameAndUser(dto.getGroupName(), currUser).isPresent()) {
                result.setValid(false);
                allErrors.add(new FieldError("contactGroupDto", "groupName", "name duplicate found"));
            }
        }

        if (result.isValid()) {
            contactService.saveGroup(dto, currUser);
        }

        return result;
    }

    @PostMapping(value = "/saveContact")
    @ResponseBody
    public AjaxResponseBody saveContact(@Valid @RequestBody ContactDto dto, BindingResult bindingResult) {

        AppUser currUser = userService.getAuthenticatedUser();

        List<FieldError> allErrors = new ArrayList<>();
        AjaxResponseBody result = new AjaxResponseBody(true, allErrors);

        if (bindingResult.hasErrors()) {
            result.setValid(false);
            allErrors.addAll(bindingResult.getFieldErrors());
        }

        if (dto.getContactId() == 0) {
            if (contactService.checkContactDuplicates(dto, currUser)) {
                result.setValid(false);
                allErrors.add(new FieldError("contactDto", "firstName", "contact duplicate found"));
            }
        }

        if (result.isValid()) {
            contactService.saveContact(dto);
        }

        return result;
    }

    @PostMapping(value = "/deleteGroup")
    @ResponseBody
    public ResponseEntity<?> deleteGroup(@RequestBody ContactGroupDto dto) {
        try {
            contactService.deleteGroupById(dto.getGroupId());
            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/deleteContact")
    @ResponseBody
    public ResponseEntity<?> deleteContact(@RequestBody ContactDto dto) {
        try {
            contactService.deleteContactById(dto.getContactId());
            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

}
