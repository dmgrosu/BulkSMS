package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmppAddressDao;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.SmppAddress;
import com.emotion.ecm.model.dto.SmppAddressDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SmppAddressService {

    private SmppAddressDao smppAddressDao;

    @Autowired
    public SmppAddressService(SmppAddressDao smppAddressDao) {
        this.smppAddressDao = smppAddressDao;
    }

    public List<SmppAddress> getAllByAccount(Account account) {
        return smppAddressDao.findAllByAccount(account);
    }

    public List<SmppAddressDto> getAllDtoByAccount(Account account) {
        return smppAddressDao.findAllByAccount(account).stream()
                .map(this::convertSmppAddressToDto).collect(Collectors.toList());
    }

    public SmppAddressDto convertSmppAddressToDto(SmppAddress smppAddress) {

        SmppAddressDto result = new SmppAddressDto();
        result.setAccountId(smppAddress.getId());
        result.setSmppAddressId(smppAddress.getId());
        result.setAddress(smppAddress.getAddress());
        result.setTon(smppAddress.getTon());
        result.setNpi(smppAddress.getNpi());

        return result;
    }
}
