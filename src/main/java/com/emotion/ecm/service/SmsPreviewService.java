package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmsPreviewDao;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.SmsPreview;
import com.emotion.ecm.model.dto.PreviewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SmsPreviewService {

    private SmsPreviewDao smsPreviewDao;

    @Autowired
    public SmsPreviewService(SmsPreviewDao smsPreviewDao) {
        this.smsPreviewDao = smsPreviewDao;
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
        result.setName(preview.getName());
        result.setPreviewId(preview.getId());
        result.setPreviewStatus(preview.getPreviewStatus().name());
        result.setPriority(preview.getSmsPriority().getName());
        result.setText(preview.getText());
        return result;
    }
}
