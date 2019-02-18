package com.emotion.ecm.service;

import com.emotion.ecm.dao.*;
import com.emotion.ecm.enums.PreviewStatus;
import com.emotion.ecm.exception.PreviewException;
import com.emotion.ecm.model.*;
import com.emotion.ecm.model.dto.AccountDto;
import com.emotion.ecm.model.dto.ContactGroupDto;
import com.emotion.ecm.model.dto.PreviewDto;
import com.emotion.ecm.model.dto.PreviewGroupDto;
import com.emotion.ecm.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SmsPreviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsPreviewService.class);

    private SmsPreviewDao smsPreviewDao;
    private AppUserDao userDao;
    private SmsTypeDao typeDao;
    private SmsPriorityDao priorityDao;
    private SmppAddressDao smppAddressDao;

    private ExpirationTimeService expirationTimeService;
    private ContactService contactService;
    private AccountDataService accountDataService;

    @Autowired
    public SmsPreviewService(SmsPreviewDao smsPreviewDao, AppUserDao userDao,
                             SmsTypeDao typeDao, SmsPriorityDao priorityDao,
                             SmppAddressDao smppAddressDao, ExpirationTimeService expirationTimeService,
                             ContactService contactService, AccountDataService accountDataService) {
        this.smsPreviewDao = smsPreviewDao;
        this.userDao = userDao;
        this.typeDao = typeDao;
        this.priorityDao = priorityDao;
        this.smppAddressDao = smppAddressDao;
        this.expirationTimeService = expirationTimeService;
        this.contactService = contactService;
        this.accountDataService = accountDataService;
    }

    public Optional<SmsPreview> getByUserIdAndName(int userId, String name) {
        return smsPreviewDao.findByUserIdAndName(userId, name);
    }

    public void deleteById(long previewId) {
        smsPreviewDao.deleteById(previewId);
    }

    public List<PreviewDto> getPreviewsForBroadcast(List<AccountDto> accounts) {

        List<PreviewDto> result = new ArrayList<>();

        List<Integer> accountIds = accounts.stream()
                .map(AccountDto::getAccountId)
                .collect(Collectors.toList());

        if (accountIds.isEmpty()) {
            return result;
        }

        List<Integer> userIds = userDao.findAllIdByAccountIds(accountIds);
        if (userIds.isEmpty()) {
            return result;
        }

        List<PreviewStatus> statuses = new ArrayList<>();
        statuses.add(PreviewStatus.APPROVED);
        statuses.add(PreviewStatus.SENDING);

        result = smsPreviewDao.findPreviewDtoForBroadcast(userIds, LocalDateTime.now(), statuses);

        if (result.isEmpty()) {
            return result;
        }

        result.stream()
                .filter(previewDto -> previewDto.getPhoneNumbers() == null || previewDto.getPhoneNumbers().isEmpty())
                .filter(previewDto -> previewDto.getAccountDataId() == null)
                .forEach(previewDto -> previewDto.setGroupIds(findAllGroupIdsByPreviewId(previewDto.getPreviewId())));

        return result;
    }

    @Transactional
    public SmsPreview save(SmsPreview preview) {
        return smsPreviewDao.save(preview);
    }

    public SmsPreview save(PreviewDto dto) {

        SmsPreview result = convertDtoToPreview(dto);

        if (dto.getPreviewId() == 0) { // new preview
            LocalDateTime now = LocalDateTime.now();
            result.setCreateDate(now);
            result.setUpdateDate(now);
            try {
                result.setRecipientsCount(getPreviewRecipientsCount(dto));
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
            result.setTotalParts(getPreviewTotalParts(dto.getText(), result.getRecipientsCount()));
        } else { // edit existing preview
            result.setUpdateDate(LocalDateTime.now());
        }

        return save(result);
    }

    @Transactional
    public SmsPreview saveAndFlush(SmsPreview preview) {
        return smsPreviewDao.saveAndFlush(preview);
    }

    public SmsPreview saveAndFlush(PreviewDto dto) {
        return saveAndFlush(convertDtoToPreview(dto));
    }

    public void changeStatus(PreviewDto previewDto) throws PreviewException {

        long previewId = previewDto.getPreviewId();

        Optional<SmsPreview> optional = smsPreviewDao.findById(previewId);
        if (optional.isPresent()) {
            SmsPreview preview = optional.get();
            preview.setPreviewStatus(previewDto.getStatus());
            smsPreviewDao.saveAndFlush(preview);
            LOGGER.info(String.format("SmsPreview[id=%s] status changed to %s", preview.getId(), preview.getPreviewStatus()));
        } else {
            throw new PreviewException(String.format("preview with id %s not found", previewId));
        }
    }

    public List<PreviewDto> getAllDtoByUserId(int id) {
        return smsPreviewDao.findAllDtoByUserId(id);
    }

    public SmsPreview getPreviewById(final long previewId) throws PreviewException {
        return smsPreviewDao.findById(previewId)
                .orElseThrow(() -> new PreviewException(String.format("preview with id %s not found", previewId)));
    }

    public PreviewDto getPreviewDtoById(long previewId) throws PreviewException {
        PreviewDto result = smsPreviewDao.findDtoById(previewId);
        if (result == null) {
            throw new PreviewException(String.format("preview with id %s not found", previewId));
        }
        String phoneNumbers = result.getPhoneNumbers();
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            if (result.getAccountDataId() == null) {
                result.setGroupIds(findAllGroupIdsByPreviewId(previewId));
            }
        }
        return result;
    }

    private SmsPreview convertDtoToPreview(PreviewDto dto) {

        SmsPreview result = smsPreviewDao.findById(dto.getPreviewId()).orElseGet(SmsPreview::new);

        result.setUser(userDao.getOne(dto.getUserId()));
        result.setName(dto.getName());
        result.setExpirationTime(expirationTimeService.getById(dto.getExpirationTimeId()));
        if (dto.getStatus() == null || dto.getStatus() == PreviewStatus.CREATED) {
            result.setPreviewStatus(PreviewStatus.CREATED);
        }
        result.setText(dto.getText());
        result.setSmsType(typeDao.getOne(dto.getTypeId()));
        result.setSmsPriority(priorityDao.getOne(dto.getPriorityId()));
        result.setTps(dto.getTps());
        result.setSmppAddress(smppAddressDao.getOne(dto.getSmppAddressId()));
        result.setDlr(dto.isDlr());
        String phoneNumbers = dto.getPhoneNumbers();
        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            result.setPhoneNumbers(phoneNumbers);
        }
        if (dto.getAccountDataId() != null) {
            accountDataService.getById(dto.getAccountDataId()).ifPresent(result::setAccountData);
        }
        Set<Integer> groupIds = dto.getGroupIds();
        if (groupIds != null && !groupIds.isEmpty()) {
            Set<Group> groups = groupIds.stream()
                    .map(groupId -> contactService.getGroupById(groupId))
                    .collect(Collectors.toSet());
            result.setGroups(groups);
        }
        result.setSendDate(dto.getSendDate());
        result.setCreateDate(dto.getCreateDate());
        result.setRecipientsCount(dto.getRecipientsCount());
        result.setTotalParts(dto.getTotalParts());
        result.setSentParts(dto.getTotalSent());

        return result;
    }

    private int getPreviewRecipientsCount(PreviewDto dto) throws IOException {
        Integer accountDataId = dto.getAccountDataId();
        Set<Integer> groupIds = dto.getGroupIds();
        String phoneNumbers = dto.getPhoneNumbers();
        if (accountDataId != null) {
            return accountDataService.getNumbersCountFromFile(accountDataId);
        } else if (groupIds != null && !groupIds.isEmpty()) {
            return contactService.getAllPhoneNumbersByGroups(groupIds).size();
        } else if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            String[] arr = phoneNumbers.split(",");
            return (int) Arrays.stream(arr)
                    .filter(s -> !s.trim().isEmpty())
                    .count();
        }
        return 0;
    }

    private int getPreviewTotalParts(String text, int recipientsCount) {
        int partsCount = StringUtil.createSmsParts(text).size();
        return recipientsCount * partsCount;
    }

    private Set<Integer> findAllGroupIdsByPreviewId(long previewId) {
        PreviewGroupDto dto = smsPreviewDao.findPreviewGroupDtoById(previewId);
        return dto.getGroups().stream()
                .map(Group::getId)
                .collect(Collectors.toSet());
    }

}
