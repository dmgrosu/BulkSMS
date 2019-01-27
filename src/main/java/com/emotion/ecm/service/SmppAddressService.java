package com.emotion.ecm.service;

import com.emotion.ecm.dao.AccountDao;
import com.emotion.ecm.dao.SmppAddressDao;
import com.emotion.ecm.exception.AccountException;
import com.emotion.ecm.exception.SmppAddressException;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.SmppAddress;
import com.emotion.ecm.model.dto.SmppAddressDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SmppAddressService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmppAddressService.class);

    private SmppAddressDao smppAddressDao;
    private AccountDao accountDao;

    @Autowired
    public SmppAddressService(SmppAddressDao smppAddressDao, AccountDao accountDao) {
        this.smppAddressDao = smppAddressDao;
        this.accountDao = accountDao;
    }

    public List<SmppAddressDto> getAllAddressesByAccount(Account account) {
        return smppAddressDao.findAllAddressesByAccount(account);
    }

    public List<SmppAddressDto> getAllDtoByAccount(Account account) throws AccountException {
        if (account == null) {
            throw new AccountException("account is null");
        }
        return smppAddressDao.findAllDtoByAccount(account);
    }

    public Optional<SmppAddress> getByAccountAndAddress(Account account, String address) {
        return smppAddressDao.findByAccountAndAddress(account, address);
    }

    @Transactional
    public SmppAddress saveSmppAddress(SmppAddress address) {
        return smppAddressDao.save(address);
    }

    public SmppAddress saveSmppAddress(SmppAddressDto dto) throws AccountException {
        return saveSmppAddress(convertDtoToSmppAddress(dto));
    }

    public SmppAddress updateSmppAddress(SmppAddressDto dto) throws AccountException {
        Optional<SmppAddress> optionalSmppAddress = smppAddressDao.findById(dto.getSmppAddressId());
        if (!optionalSmppAddress.isPresent()) {
            throw new AccountException(String.format("SMPP address with id %s not found", dto.getSmppAddressId()));
        }
        SmppAddress smppAddress = optionalSmppAddress.get();
        boolean changed = false;
        if (!dto.getAddress().equals(smppAddress.getAddress())) {
            changed = true;
            smppAddress.setAddress(dto.getAddress());
        }
        if (dto.getTon() != smppAddress.getTon()) {
            changed = true;
            smppAddress.setTon(dto.getTon());
        }
        if (dto.getNpi() != smppAddress.getNpi()) {
            changed = true;
            smppAddress.setNpi(dto.getNpi());
        }

        if (changed) {
            saveSmppAddress(smppAddress);
        }
        return smppAddress;
    }

    @Transactional
    public void deleteById(int smppAddressId) {
        smppAddressDao.deleteById(smppAddressId);
    }

    public SmppAddressDto getById(int id) throws SmppAddressException {
        return smppAddressDao.findDtoById(id);
    }

    private SmppAddress convertDtoToSmppAddress(SmppAddressDto dto) throws AccountException {

        SmppAddress result = new SmppAddress();
        int accountId = dto.getAccountId();
        Optional<Account> optionalAccount = accountDao.findById(accountId);
        if (optionalAccount.isPresent()) {
            result.setAccount(optionalAccount.get());
            result.setAddress(dto.getAddress());
            result.setTon(dto.getTon());
            result.setNpi(dto.getNpi());
        } else {
            throw new AccountException(String.format("Account with id %s not found", accountId));
        }

        return result;
    }

}
