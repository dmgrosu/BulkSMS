package com.emotion.ecm.service;

import com.emotion.ecm.dao.*;
import com.emotion.ecm.enums.PreviewStatus;
import com.emotion.ecm.model.Account;
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

@Service
public class SmsPreviewService {

    private SmsPreviewDao smsPreviewDao;
    private AccountDao accountDao;
    private AppUserDao userDao;
    private SmsTypeDao typeDao;
    private SmsPriorityDao priorityDao;

    @Autowired
    public SmsPreviewService(SmsPreviewDao smsPreviewDao, AccountDao accountDao,
                             AppUserDao userDao, SmsTypeDao typeDao,
                             SmsPriorityDao priorityDao) {
        this.smsPreviewDao = smsPreviewDao;
        this.accountDao = accountDao;
        this.userDao = userDao;
        this.typeDao = typeDao;
        this.priorityDao = priorityDao;
    }

    public List<SmsPreview> getAllByAccountAndUser(Account account, AppUser user) {
        return smsPreviewDao.findAllByAccountAndUser(account, user);
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
        result.setAccountId(preview.getAccount().getId());
        result.setAccountName(preview.getAccount().getName());
        result.setUsername(preview.getUser().getUsername());
        result.setUserId(preview.getUser().getId());
        result.setName(preview.getName());
        result.setPreviewId(preview.getId());
        result.setPreviewStatus(preview.getPreviewStatus().name());
        result.setPriority(preview.getSmsPriority().getName());
        result.setText(preview.getText());
        result.setSendDate(DateUtil.formatDate(preview.getSendDate()));
        result.setCreateDate(DateUtil.formatDate(preview.getCreateDate()));
        return result;
    }

    public SmsPreview convertDtoToPreview(PreviewDto dto) throws ParseException {

        SmsPreview result = new SmsPreview();

        result.setAccount(accountDao.getOne(dto.getAccountId()));
        result.setUser(userDao.getOne(dto.getUserId()));
        result.setName(dto.getName());
        result.setExpirationTime(dto.getExpirationTime());
        result.setPreviewStatus(PreviewStatus.CREATED);
        result.setText(dto.getText());
        result.setSmsType(typeDao.getOne(dto.getTypeId()));
        result.setSmsPriority(priorityDao.getOne(dto.getPriorityId()));
        result.setTps(dto.getTps());
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
}
