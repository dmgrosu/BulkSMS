package com.emotion.ecm.service;

import com.emotion.ecm.enums.MessageStatus;
import com.emotion.ecm.enums.PreviewStatus;
import com.emotion.ecm.model.*;
import com.emotion.ecm.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class SmsGeneration implements Runnable {

    private SmsPreviewService smsPreviewService;
    private SmsMessageService smsMessageService;
    private SmsPreview preview;
    private SmsTextService smsTextService;

    @Override
    public void run() {
        if (preview != null) {
            startGenerationProcess();
        }
    }

    private void startGenerationProcess() {

        List<SmsMessage> messages = new ArrayList<>();

        List<String> numbersList = null;
        AccountData accountData = preview.getAccountData();
        Set<Group> groups = preview.getGroups();
        Set<Contact> contacts = preview.getContacts();
        if (accountData != null) {
            numbersList = getNumbersFromFile(accountData);
        } else if (groups != null) {
            numbersList = getNumbersFromGroup(groups);
        } else if (contacts != null) {
            numbersList = getNumbersFromContacts(contacts);
        }

        if (numbersList != null && !numbersList.isEmpty()) {

            List<String> smsParts = StringUtil.createSmsParts(preview.getText(), false);

            SmsMessage message;
            for (String destAddr : numbersList) {
                for (short i = 1; i <= smsParts.size(); i++) {
                    String smsPart = smsParts.get(i);
                    message = new SmsMessage();
                    message.setDestAddress(destAddr);
                    message.setMessageStatus(MessageStatus.READY);
                    message.setPreview(preview);
                    message.setSmsText(smsTextService.getByTextAndParts(smsPart, i, (short)smsParts.size()));
                    messages.add(message);
                }
            }

            preview.setRecipientsCount(numbersList.size());
            preview.setPreviewStatus(PreviewStatus.GENERATING);
            preview = smsPreviewService.save(preview);

        }

        if (!messages.isEmpty()) {
            messages = smsMessageService.batchSave(messages);
        }

        if (messages != null && messages.size() > 0) {
            preview.setPreviewStatus(PreviewStatus.APPROVED);
            smsPreviewService.save(preview);
        }

    }

    private List<String> getNumbersFromContacts(Set<Contact> contacts) {
        return null;
    }

    private List<String> getNumbersFromGroup(Set<Group> groups) {
        return null;
    }

    private List<String> getNumbersFromFile(AccountData accountData) {
        return null;
    }


}
