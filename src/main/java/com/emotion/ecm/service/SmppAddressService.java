package com.emotion.ecm.service;

import com.emotion.ecm.dao.AccountDao;
import com.emotion.ecm.dao.SmppAddressDao;
import com.emotion.ecm.exception.AccountException;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.SmppAddress;
import com.emotion.ecm.model.dto.SmppAddressDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SmppAddressService {

    private SmppAddressDao smppAddressDao;
    private AccountDao accountDao;

    @Autowired
    public SmppAddressService(SmppAddressDao smppAddressDao, AccountDao accountDao) {
        this.smppAddressDao = smppAddressDao;
        this.accountDao = accountDao;
    }

    public List<SmppAddress> getAllByAccount(Account account) {
        return smppAddressDao.findAllByAccount(account);
    }

    public List<SmppAddressDto> getAllDtoByAccount(Account account) {
        return smppAddressDao.findAllByAccount(account).stream()
                .map(this::convertSmppAddressToDto).collect(Collectors.toList());
    }

    public Optional<SmppAddress> getByAccountAndAddress(Account account, String address) {
        return smppAddressDao.findByAccountAndAddress(account, address);
    }

    public SmppAddressDto convertSmppAddressToDto(SmppAddress smppAddress) {

        SmppAddressDto result = new SmppAddressDto();
        result.setAccountId(smppAddress.getAccount().getId());
        result.setSmppAddressId(smppAddress.getId());
        result.setAddress(smppAddress.getAddress());
        result.setTon(smppAddress.getTon());
        result.setNpi(smppAddress.getNpi());

        return result;
    }

    public SmppAddress saveNewSmppAddress(SmppAddressDto dto) throws AccountException {
        return smppAddressDao.save(convertDtoToSmppAddress(dto));
    }

    public SmppAddress convertDtoToSmppAddress(SmppAddressDto dto) throws AccountException {

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
            smppAddressDao.save(smppAddress);
        }
        return smppAddress;
    }

    public void deleteById(int smppAddressId) {
        smppAddressDao.deleteById(smppAddressId);
    }
}
