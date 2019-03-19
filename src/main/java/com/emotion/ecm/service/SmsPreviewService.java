package com.emotion.ecm.service;

import com.emotion.ecm.dao.*;
import com.emotion.ecm.enums.PreviewStatus;
import com.emotion.ecm.exception.PreviewException;
import com.emotion.ecm.model.*;
import com.emotion.ecm.model.dto.*;
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

    @Transactional
    public SmsPreview save(SmsPreview preview) {
        return smsPreviewDao.save(preview);
    }

    public SmsPreview save(PreviewDto dto) {

        SmsPreview result = convertDtoToPreview(dto);

        LocalDateTime now = LocalDateTime.now();

        if (dto.getPreviewId() == 0) { // new preview
            result.setCreateDate(now);
            try {
                result.setRecipientsCount(getPreviewRecipientsCount(dto));
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }

        result.setTotalParts(getPreviewTotalParts(dto.getText(), result.getRecipientsCount()));
        result.setUpdateDate(now);

        return save(result);
    }

    @Transactional
    public SmsPreview saveAndFlush(SmsPreview preview) {
        return smsPreviewDao.saveAndFlush(preview);
    }

    public SmsPreview saveAndFlush(PreviewDto dto) {
        return saveAndFlush(convertDtoToPreview(dto));
    }

    @Transactional
    public void changeStatus(PreviewDto previewDto) throws PreviewException {

        long previewId = previewDto.getPreviewId();

        Optional<SmsPreview> optional = smsPreviewDao.findById(previewId);
        if (optional.isPresent()) {
            smsPreviewDao.updatePreviewStatusById(previewId, previewDto.getStatus());
            LOGGER.info(String.format("SmsPreview[id=%s] status changed to %s", previewId, previewDto.getStatus()));
        } else {
            throw new PreviewException(String.format("preview with id %s not found", previewId));
        }
    }

    public List<PreviewDto> getAllDtoByUserId(int id, boolean showFinished) {
        if (showFinished) {
            return smsPreviewDao.findAllDtoByUserId(id);
        } else {
            return smsPreviewDao.findAllNotFinishedDtoByUserId(id);
        }
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

    SmsPreview getPreviewById(final long previewId) throws PreviewException {
        return smsPreviewDao.findById(previewId)
                .orElseThrow(() -> new PreviewException(String.format("preview with id %s not found", previewId)));
    }

    /**
     * @param accounts list of Account
     * @return map: key - PreviewDto, value - id of smscAccount
     */
    Map<PreviewDto, Integer> getPreviewsForBroadcast(List<AccountDto> accounts) {

        Map<PreviewDto, Integer> result = new HashMap<>();

        // key - accountId
        // value - SMSCAccountId
        Map<Integer, Integer> accountSmsc = accounts.stream()
                .collect(Collectors.toMap(AccountDto::getAccountId, AccountDto::getSmscAccountId, (a, b) -> b));

        Set<Integer> accountIds = accountSmsc.keySet();

        if (accountIds.isEmpty()) {
            return result;
        }

        List<UserDto> userList = userDao.findAllDtoByAccountIds(accountIds);
        if (userList.isEmpty()) {
            return result;
        }

        // key - userId
        // value - SMSCAccountId
        Map<Integer, Integer> userSmsc = new HashMap<>();
        for (UserDto userDto : userList) {
            userSmsc.put(userDto.getUserId(), accountSmsc.get(userDto.getAccountId()));
        }

        List<Integer> userIds = userList.stream()
                .map(UserDto::getUserId)
                .collect(Collectors.toList());

        List<PreviewStatus> statuses = new ArrayList<>();
        statuses.add(PreviewStatus.APPROVED);
        statuses.add(PreviewStatus.SENDING);

        List<PreviewDto> previewDtoList = smsPreviewDao.findPreviewDtoForBroadcast(userIds, LocalDateTime.now(), statuses);
        previewDtoList.stream()
                .filter(previewDto -> previewDto.getPhoneNumbers() == null || previewDto.getPhoneNumbers().isEmpty())
                .filter(previewDto -> previewDto.getAccountDataId() == null)
                .forEach(previewDto -> previewDto.setGroupIds(findAllGroupIdsByPreviewId(previewDto.getPreviewId())));

        if (previewDtoList.isEmpty()) {
            return result;
        }

        for (PreviewDto previewDto : previewDtoList) {
            result.put(previewDto, userSmsc.get(previewDto.getUserId()));
        }

        return result;
    }

    private SmsPreview convertDtoToPreview(PreviewDto dto) {

        SmsPreview result = smsPreviewDao.findById(dto.getPreviewId()).orElseGet(SmsPreview::new);

        if (dto.getSendDate() != result.getSendDate()) {
            result.setSendDate(dto.getSendDate());
        }
        if (!dto.getName().equalsIgnoreCase(result.getName())) {
            result.setName(dto.getName());
        }
        if (dto.getStatus() == null || dto.getStatus() == PreviewStatus.CREATED) {
            result.setPreviewStatus(PreviewStatus.CREATED);
        }
        if (!dto.getText().equals(result.getText())) {
            result.setText(dto.getText());
        }
        if (dto.getTps() != result.getTps()) {
            result.setTps(dto.getTps());
        }
        if (dto.isDlr() != result.isDlr()) {
            result.setDlr(dto.isDlr());
        }
        result.setUser(userDao.getOne(dto.getUserId()));
        result.setExpirationTime(expirationTimeService.getById(dto.getExpirationTimeId()));
        result.setSmsType(typeDao.getOne(dto.getTypeId()));
        result.setSmsPriority(priorityDao.getOne(dto.getPriorityId()));
        result.setSmppAddress(smppAddressDao.getOne(dto.getSmppAddressId()));

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

        if (result.getRecipientsCount() != dto.getRecipientsCount()) {
            result.setRecipientsCount(dto.getRecipientsCount());
        }
        if (result.getTotalParts() != dto.getTotalParts()) {
            result.setTotalParts(dto.getTotalParts());
        }
        if (result.getSentParts() != dto.getTotalSent()) {
            result.setSentParts(dto.getTotalSent());
        }

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

    @Transactional
    public void updatePreviewToCompletedById(long previewId) {
        smsPreviewDao.updatePreviewToCompleted(previewId, LocalDateTime.now(), PreviewStatus.FINISHED);
    }

    @Transactional
    public void updatePreviewSentCountById(long previewId, int sentCount) {
        smsPreviewDao.updatePreviewSentCountById(previewId, sentCount, PreviewStatus.SENDING);
    }
}
