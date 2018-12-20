package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmsTextDao;
import com.emotion.ecm.model.SmsText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SmsTextService {

    private SmsTextDao smsTextDao;

    @Autowired
    public SmsTextService(SmsTextDao smsTextDao) {
        this.smsTextDao = smsTextDao;
    }

    public SmsText getByTextAndParts(String text, short part, short totalParts) {
        Optional<SmsText> optional = smsTextDao.findByTextAndPartAndTotalParts(text, part, totalParts);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            SmsText smsText = new SmsText();
            smsText.setText(text);
            smsText.setPart(part);
            smsText.setTotalParts(totalParts);
            return smsTextDao.save(smsText);
        }
    }

}
