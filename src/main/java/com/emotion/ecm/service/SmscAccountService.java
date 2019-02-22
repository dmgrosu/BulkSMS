package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmscAccountDao;
import com.emotion.ecm.exception.SmscAccountException;
import com.emotion.ecm.model.SmscAccount;
import com.emotion.ecm.model.dto.SmscAccountDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SmscAccountService {

    private SmscAccountDao smscAccountDao;

    @Autowired
    public SmscAccountService(SmscAccountDao smscAccountDao) {
        this.smscAccountDao = smscAccountDao;
    }

    public List<SmscAccountDto> getAllDto() {
        return smscAccountDao.findAllDto();
    }

    public List<SmscAccountDto> getAllNames() {
        return smscAccountDao.findAllNames();
    }

    public boolean checkDuplicate(SmscAccountDto smscAccountDto) {
        if (smscAccountDto.getSmscAccountId() != 0) {
            return false;
        } else {
            String ipAddress = smscAccountDto.getIpAddress();
            int port = smscAccountDto.getPort();
            return smscAccountDao.findByIpAddressAndPort(ipAddress, port).isPresent();
        }
    }

    public SmscAccount save(SmscAccountDto smscAccountDto) {
        return save(convertDtoToSmscAccount(smscAccountDto));
    }

    @Transactional
    public SmscAccount save(SmscAccount smscAccount) {
        return smscAccountDao.save(smscAccount);
    }

    public void deleteById(int smscAccountId) {
        smscAccountDao.deleteById(smscAccountId);
    }

    public SmscAccount getById(int smscAccountId) {
        return smscAccountDao.findById(smscAccountId).orElse(null);
    }

    public SmscAccountDto getDtoById(int smscAccountId) throws SmscAccountException {
        return smscAccountDao.findDtoById(smscAccountId);
    }

    public Integer getTpsById(int smscAccountId) {
        return smscAccountDao.findTpsById(smscAccountId);
    }

    private SmscAccount convertDtoToSmscAccount(SmscAccountDto dto) {

        Optional<SmscAccount> optional = smscAccountDao.findById(dto.getSmscAccountId());
        SmscAccount result = optional.orElseGet(SmscAccount::new);

        result.setSystemId(dto.getSystemId());
        result.setPassword(dto.getPassword());
        result.setIpAddress(dto.getIpAddress());
        result.setPort(dto.getPort());
        result.setTps(dto.getTps());
        result.setAsynchronous(dto.isAsynchronous());
        result.setMaxConnections(dto.getMaxConnections());

        return result;
    }

}
