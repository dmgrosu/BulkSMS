package com.emotion.ecm.service;

import com.emotion.ecm.dao.AccountDao;
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

    public List<SmscAccount> getAllByAccount(Account account) {
        return smscAccountDao.findAllByAccount(account);
    }

    public SmscAccount convertDtoToSmscAccount(SmscAccountDto dto, Account account) {

        SmscAccount result = new SmscAccount();
        result.setAccount(account);
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
        result.setAccountId(smscAccount.getAccount().getId());
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

        return getAllByAccount(account).stream()
                .map(this::convertSmscAccountToDto).collect(Collectors.toList());
    }

    public boolean checkDuplicate(SmscAccountDto smscAccountDto) {
        if (smscAccountDto.getSmscAccountId() != 0) {
            return false;
        } else {
            int accountId = smscAccountDto.getAccountId();
            String ipAddress = smscAccountDto.getIpAddress();
            int port = smscAccountDto.getPort();
            return smscAccountDao.findByAccountIdAndIpAddressAndPort(accountId, ipAddress, port).isPresent();
        }
    }

    public SmscAccount save(SmscAccountDto smscAccountDto, Account account) {
        Optional<SmscAccount> optional = smscAccountDao.findById(smscAccountDto.getAccountId());
        SmscAccount result = optional.orElseGet(SmscAccount::new);
        SmscAccount converted = convertDtoToSmscAccount(smscAccountDto, account);
        if (!result.equals(converted)) {
            result = smscAccountDao.save(converted);
        }
        return result;
    }

    public void deleteById(int smscAccountId) {
        smscAccountDao.deleteById(smscAccountId);
    }

}
