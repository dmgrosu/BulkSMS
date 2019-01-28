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

    public SmppAddress saveSmppAddress(SmppAddressDto dto, Account currAccount) throws AccountException {
        return saveSmppAddress(convertDtoToSmppAddress(dto, currAccount));
    }

    @Transactional
    public void deleteById(int smppAddressId) {
        smppAddressDao.deleteById(smppAddressId);
    }

    public SmppAddressDto getDtoById(int id) throws SmppAddressException {
        return smppAddressDao.findDtoById(id);
    }

    private SmppAddress convertDtoToSmppAddress(SmppAddressDto dto, Account currAccount) throws AccountException {

        if (currAccount == null) {
            Optional<Account> optionalAccount = accountDao.findById(dto.getAccountId());
            if (optionalAccount.isPresent()) {
                currAccount = optionalAccount.get();
            } else {
                throw new AccountException(String.format("Account with id %s not found", dto.getAccountId()));
            }
        }

        SmppAddress result = new SmppAddress();
        result.setAccount(currAccount);
        result.setAddress(dto.getAddress());
        result.setTon(dto.getTon());
        result.setNpi(dto.getNpi());

        return result;
    }

}
