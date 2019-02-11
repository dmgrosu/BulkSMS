package com.emotion.ecm.service;

import com.emotion.ecm.dao.*;
import com.emotion.ecm.enums.PreviewStatus;
import com.emotion.ecm.exception.PreviewException;
import com.emotion.ecm.model.*;
import com.emotion.ecm.model.dto.AccountDto;
import com.emotion.ecm.model.dto.ContactGroupDto;
import com.emotion.ecm.model.dto.PreviewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SmsPreviewService {

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

    public SmsPreview createNewPreview(PreviewDto dto) {

        SmsPreview result = convertDtoToPreview(dto);

        LocalDateTime now = LocalDateTime.now();
        result.setCreateDate(now);
        result.setUpdateDate(now);

        result = save(result);

        return result;
    }

    public void deleteById(long previewId) {
        smsPreviewDao.deleteById(previewId);
    }

    public List<PreviewDto> getPreviewsForBroadcast(List<AccountDto> accounts) {

        List<PreviewDto> result = new ArrayList<>();

        List<Integer> accountIds = accounts.stream()
                .map(AccountDto::getAccountId).collect(Collectors.toList());
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

        for (PreviewDto previewDto : result) {
            Set<Integer> groupIds = contactService.getAllGroupDtoByUserId(previewDto.getUserId(), false)
                    .stream()
                    .map(ContactGroupDto::getGroupId)
                    .collect(Collectors.toSet());
            previewDto.setGroupIds(groupIds);
        }

        return result;
    }

    @Transactional
    public SmsPreview save(SmsPreview preview) {
        return smsPreviewDao.save(preview);
    }

    public SmsPreview save(PreviewDto dto) {
        return save(convertDtoToPreview(dto));
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

    private SmsPreview convertDtoToPreview(PreviewDto dto) {

        SmsPreview result = new SmsPreview();

        result.setUser(userDao.getOne(dto.getUserId()));
        result.setName(dto.getName());
        result.setExpirationTime(expirationTimeService.getById(dto.getExpirationTimeId()));
        result.setPreviewStatus(PreviewStatus.CREATED);
        result.setText(dto.getText());
        result.setSmsType(typeDao.getOne(dto.getTypeId()));
        result.setSmsPriority(priorityDao.getOne(dto.getPriorityId()));
        result.setTps(dto.getTps());
        result.setSmppAddress(smppAddressDao.getOne(dto.getSmppAddressId()));
        result.setDlr(dto.isDlr());
        result.setPhoneNumbers(dto.getPhoneNumbers());
        if (dto.getAccountDataId() != 0) {
            accountDataService.getById(dto.getAccountDataId()).ifPresent(result::setAccountData);
        }
        Set<Integer> groupIds = dto.getGroupIds();
        if (groupIds != null && groupIds.isEmpty()) {
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

}
