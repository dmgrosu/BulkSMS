package com.emotion.ecm.service;

import com.emotion.ecm.dao.SmscAccountDao;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.SmscAccount;
import com.emotion.ecm.model.dto.SmscAccountDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SmscAccountService {

    private SmscAccountDao smscAccountDao;

    @Autowired
    public SmscAccountService(SmscAccountDao smscAccountDao) {
        this.smscAccountDao = smscAccountDao;
    }

    public List<SmscAccount> getAll() {
        return smscAccountDao.findAll();
    }

    public SmscAccount convertDtoToSmscAccount(SmscAccountDto dto) {

        SmscAccount result = new SmscAccount();
        result.setSystemId(dto.getSystemId());
        result.setPassword(dto.getPassword());
        result.setIpAddress(dto.getIpAddress());
        result.setPort(dto.getPort());
        result.setTps(dto.getTps());
        result.setAsynchronous(dto.isAsynchronous());
        result.setMaxConnections(dto.getMaxConnections());

        return result;
    }

    public SmscAccountDto convertSmscAccountToDto(SmscAccount smscAccount) {

        SmscAccountDto result = new SmscAccountDto();
        result.setSmscAccountId(smscAccount.getId());
        result.setSystemId(smscAccount.getSystemId());
        result.setPassword(smscAccount.getPassword());
        result.setIpAddress(smscAccount.getIpAddress());
        result.setPort(smscAccount.getPort());
        result.setTps(smscAccount.getTps());
        result.setAsynchronous(smscAccount.isAsynchronous());
        result.setMaxConnections(smscAccount.getMaxConnections());

        return result;
    }

    public List<SmscAccountDto> getDtoListByAccount(Account account) {

        return getAll().stream()
                .map(this::convertSmscAccountToDto).collect(Collectors.toList());
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
        Optional<SmscAccount> optional = smscAccountDao.findById(smscAccountDto.getSmscAccountId());
        SmscAccount result = optional.orElseGet(SmscAccount::new);
        SmscAccount converted = convertDtoToSmscAccount(smscAccountDto);
        if (!result.equals(converted)) {
            result = smscAccountDao.save(converted);
        }
        return result;
    }

    public void deleteById(int smscAccountId) {
        smscAccountDao.deleteById(smscAccountId);
    }

}
