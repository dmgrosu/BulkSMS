package com.emotion.ecm.service;

import com.emotion.ecm.dao.*;
import com.emotion.ecm.enums.PreviewStatus;
import com.emotion.ecm.exception.PreviewException;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.Group;
import com.emotion.ecm.model.SmsPreview;
import com.emotion.ecm.model.dto.PreviewDto;
import com.emotion.ecm.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.ParseException;
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
    private SmsMessageService smsMessageService;
    private SmsTextService smsTextService;
    private ContactService contactService;
    private AccountDataService accountDataService;
    private SmsPrefixService smsPrefixService;

    @Autowired
    public SmsPreviewService(SmsPreviewDao smsPreviewDao, AppUserDao userDao,
                             SmsTypeDao typeDao, SmsPriorityDao priorityDao,
                             SmppAddressDao smppAddressDao, ExpirationTimeService expirationTimeService,
                             SmsMessageService smsMessageService, SmsTextService smsTextService,
                             ContactService contactService, AccountDataService accountDataService,
                             SmsPrefixService smsPrefixService) {
        this.smsPreviewDao = smsPreviewDao;
        this.userDao = userDao;
        this.typeDao = typeDao;
        this.priorityDao = priorityDao;
        this.smppAddressDao = smppAddressDao;
        this.expirationTimeService = expirationTimeService;
        this.smsMessageService = smsMessageService;
        this.smsTextService = smsTextService;
        this.contactService = contactService;
        this.accountDataService = accountDataService;
        this.smsPrefixService = smsPrefixService;
    }

    public List<SmsPreview> getAllByUser(AppUser user) {
        return smsPreviewDao.findAllByUser(user);
    }

    public Optional<SmsPreview> getByUserIdAndName(int userId, String name) {
        return smsPreviewDao.findByUserIdAndName(userId, name);
    }

    public List<PreviewDto> convertPreviewListToDto(List<SmsPreview> smsPreviews) {
        List<PreviewDto> result = new ArrayList<>();
        for (SmsPreview smsPreview : smsPreviews) {
            result.add(convertPreviewToDto(smsPreview));
        }
        return result;
    }

    public PreviewDto convertPreviewToDto(SmsPreview preview) {
        PreviewDto result = new PreviewDto();
        result.setPreviewId(preview.getId());
        result.setUsername(preview.getUser().getUsername());
        result.setUserId(preview.getUser().getId());
        result.setName(preview.getName());
        result.setStatus(preview.getPreviewStatus().name());
        result.setPriority(preview.getSmsPriority().getName());
        result.setText(preview.getText());
        result.setTps(preview.getTps());
        result.setSendDate(DateUtil.formatDate(preview.getSendDate()));
        result.setCreateDate(DateUtil.formatDate(preview.getCreateDate()));
        result.setSmppAddress(preview.getSmppAddress().getAddress());
        result.setExpirationTimeId(preview.getExpirationTime().getId());
        result.setExpirationTimeName(preview.getExpirationTime().getName());
        result.setAccountDataId(preview.getAccountData().getId());
        result.setAccountDataName(preview.getAccountData().getName());
        return result;
    }

    public SmsPreview convertDtoToPreview(PreviewDto dto) throws ParseException {

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
            result.setAccountData(accountDataService.getById(dto.getAccountDataId()));
        }
        Set<Integer> groupIds = dto.getGroupIds();
        if (groupIds != null && groupIds.isEmpty()) {
            Set<Group> groups = groupIds.stream()
                    .map(groupId -> contactService.getGroupById(groupId))
                    .collect(Collectors.toSet());
            result.setGroups(groups);
        }
        if (!StringUtils.isEmpty(dto.getSendDate())) {
            result.setSendDate(DateUtil.parseDate(dto.getSendDate()));
        }
        if (!StringUtils.isEmpty(dto.getCreateDate())) {
            result.setCreateDate(DateUtil.parseDate(dto.getCreateDate()));
        }

        return result;
    }

    @Transactional
    public SmsPreview createNewPreview(PreviewDto dto) throws ParseException {

        SmsPreview result = convertDtoToPreview(dto);

        LocalDateTime now = LocalDateTime.now();
        result.setCreateDate(now);
        result.setUpdateDate(now);

        result = smsPreviewDao.save(result);

        return result;
    }

    @Transactional
    public void deleteById(long previewId) {
        smsPreviewDao.deleteById(previewId);
    }

    public List<SmsPreview> getPreviewsForBroadcast(List<AppUser> users) {

        List<Integer> userIds = users.stream()
                .map(AppUser::getId).collect(Collectors.toList());
        List<PreviewStatus> statuses = new ArrayList<>();
        statuses.add(PreviewStatus.APPROVED);
        statuses.add(PreviewStatus.SENDING);

        return smsPreviewDao.findPreviewsForBroadcast(userIds, LocalDateTime.now(), statuses);
    }

    @Transactional
    public SmsPreview save(SmsPreview preview) {
        return smsPreviewDao.save(preview);
    }

    @Transactional
    public SmsPreview saveAndFlush(SmsPreview preview) {
        return smsPreviewDao.saveAndFlush(preview);
    }

    @Transactional
    public void changeStatus(PreviewDto previewDto) throws PreviewException {

        long previewId = previewDto.getPreviewId();

        Optional<SmsPreview> optional = smsPreviewDao.findById(previewId);
        if (optional.isPresent()) {
            SmsPreview preview = optional.get();
            preview.setPreviewStatus(PreviewStatus.valueOf(previewDto.getStatus()));
            preview = smsPreviewDao.saveAndFlush(preview);
            if (preview.getPreviewStatus() == PreviewStatus.APPROVED) {
                startGenerationProcess(preview);
            }
        } else {
            throw new PreviewException(String.format("preview with id %s not found", previewId));
        }

    }

    private void startGenerationProcess(SmsPreview preview) {

        if (preview != null) {
            SmsGeneration smsGeneration = new SmsGeneration(this, smsMessageService,
                    preview, smsTextService, accountDataService, contactService, smsPrefixService);
            smsGeneration.startGenerationProcess();
        }

    }
}
