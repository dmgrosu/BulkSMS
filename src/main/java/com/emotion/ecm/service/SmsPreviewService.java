package com.emotion.ecm.service;

import com.emotion.ecm.dao.*;
import com.emotion.ecm.enums.PreviewStatus;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmsPreview;
import com.emotion.ecm.model.dto.PreviewDto;
import com.emotion.ecm.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SmsPreviewService {

    private SmsPreviewDao smsPreviewDao;
    private AppUserDao userDao;
    private SmsTypeDao typeDao;
    private SmsPriorityDao priorityDao;
    private SmppAddressDao smppAddressDao;
    private ExpirationTimeService expirationTimeService;

    @Autowired
    public SmsPreviewService(SmsPreviewDao smsPreviewDao, AppUserDao userDao,
                             SmsTypeDao typeDao, SmsPriorityDao priorityDao,
                             SmppAddressDao smppAddressDao, ExpirationTimeService expirationTimeService) {
        this.smsPreviewDao = smsPreviewDao;
        this.userDao = userDao;
        this.typeDao = typeDao;
        this.priorityDao = priorityDao;
        this.smppAddressDao = smppAddressDao;
        this.expirationTimeService = expirationTimeService;
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
        if (!StringUtils.isEmpty(dto.getSendDate())) {
            result.setSendDate(DateUtil.parseDate(dto.getSendDate()));
        }
        if (!StringUtils.isEmpty(dto.getCreateDate())) {
            result.setCreateDate(DateUtil.parseDate(dto.getCreateDate()));
        }

        return result;
    }

    public SmsPreview createNewPreview(PreviewDto dto) throws ParseException {

        SmsPreview result = convertDtoToPreview(dto);

        LocalDateTime now = LocalDateTime.now();
        result.setCreateDate(now);
        result.setUpdateDate(now);

        return smsPreviewDao.save(result);
    }

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

    public SmsPreview save(SmsPreview preview) {
        return smsPreviewDao.save(preview);
    }
}
