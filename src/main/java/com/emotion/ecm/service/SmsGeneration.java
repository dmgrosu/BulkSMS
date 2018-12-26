package com.emotion.ecm.service;

import com.emotion.ecm.enums.MessageStatus;
import com.emotion.ecm.enums.PreviewStatus;
import com.emotion.ecm.model.*;
import com.emotion.ecm.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class SmsGeneration {

    private SmsPreviewService smsPreviewService;
    private SmsMessageService smsMessageService;
    private SmsPreview preview;
    private SmsTextService smsTextService;
    private AccountDataService accountDataService;
    private ContactService contactService;
    private SmsPrefixService smsPrefixService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsGeneration.class);

    @Async
    public void startGenerationProcess() {

        List<SmsMessage> messages = new ArrayList<>();

        List<String> numbersList = null;
        AccountData accountData = preview.getAccountData();
        Set<Group> groups = preview.getGroups();
        String phoneNumbers = preview.getPhoneNumbers();
        if (accountData != null) {
            numbersList = getNumbersFromFile(accountData);
        } else if (groups != null && !groups.isEmpty()) {
            numbersList = getNumbersFromGroup(groups);
        } else if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            numbersList = getNumbersFromString(phoneNumbers);
        }

        if (numbersList != null && !numbersList.isEmpty()) {

            List<SmsPrefix> prefixes = smsPrefixService.getAllPrefixesByAccount(preview.getUser().getAccount());

            List<SmsText> smsParts = createSmsParts(preview.getText());

            SmsMessage message;
            for (String destAddr : numbersList) {
                SmsPrefix prefix = getPrefixForMsisdn(prefixes, destAddr);
                for (SmsText smsPart : smsParts) {
                    message = new SmsMessage();
                    message.setDestAddress(destAddr);
                    message.setMessageStatus(MessageStatus.READY);
                    message.setPreview(preview);
                    message.setSmsText(smsPart);
                    message.setSmsPrefix(prefix);
                    messages.add(message);
                }
            }

            preview.setRecipientsCount(numbersList.size());
            preview.setPreviewStatus(PreviewStatus.GENERATING);
            preview = smsPreviewService.saveAndFlush(preview);

        }

        if (!messages.isEmpty()) {
            messages = smsMessageService.batchSave(messages);
        }

        if (messages != null && messages.size() > 0) {
            preview.setPreviewStatus(PreviewStatus.APPROVED);
            preview.setTotalParts(messages.size());
            smsPreviewService.saveAndFlush(preview);
        }

    }

    private List<SmsText> createSmsParts(String initialText) {

        List<SmsText> result = new ArrayList<>();

        List<String> smsParts = StringUtil.createSmsParts(initialText, false);
        short partNumber = 1;
        for (String smsPart : smsParts) {
            result.add(smsTextService.getByTextAndParts(smsPart, partNumber, (short) smsParts.size()));
            partNumber++;
        }

        return result;
    }

    private SmsPrefix getPrefixForMsisdn(List<SmsPrefix> prefixes, String destAddr) {
        for (SmsPrefix prefix : prefixes) {
            if (destAddr.startsWith(prefix.getPrefix())) {
                return prefix;
            }
        }
        return null;
    }

    private List<String> getNumbersFromString(String phoneNumbers) {

        List<String> result = new ArrayList<>();

        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            String[] arr = phoneNumbers.split(",");
            result.addAll(Arrays.asList(arr));
        }

        return result;
    }

    private List<String> getNumbersFromGroup(Set<Group> groups) {

        return contactService.getAllContactsByGroups(groups).stream()
                .map(Contact::getMobilePhone)
                .collect(Collectors.toList());
    }

    private List<String> getNumbersFromFile(AccountData accountData) {

        List<String> result = new ArrayList<>();

        try {
            Path directory = accountDataService.getAccountPath(preview.getUser());
            Path fullPath = directory.resolve(accountData.getFileName());
            BufferedReader reader = Files.newBufferedReader(fullPath);
            String currLine;
            while ((currLine = reader.readLine()) != null) {
                result.add(currLine);
            }
            reader.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return result;
        }

        return result;
    }


}
