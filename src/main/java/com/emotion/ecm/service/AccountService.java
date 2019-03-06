package com.emotion.ecm.service;

import com.emotion.ecm.dao.AccountDao;
import com.emotion.ecm.exception.AccountException;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.dto.AccountDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private AccountDao accountDao;
    private SmscAccountService smscAccountService;

    @Autowired
    public AccountService(AccountDao accountDao, SmscAccountService smscAccountService) {
        this.accountDao = accountDao;
        this.smscAccountService = smscAccountService;
    }

    public List<Account> getAll() {
        return accountDao.findAll();
    }

    public List<AccountDto> getAllDto() {
        return accountDao.getAllDto();
    }

    public List<AccountDto> getAllNames() {
        return accountDao.getAllNames();
    }

    public Account getById(int id) throws AccountException {
        return accountDao.findById(id)
                .orElseThrow(() -> new AccountException("account not found"));
    }

    public Account save(AccountDto dto) {
        return accountDao.save(convertDtoToAccount(dto));
    }

    public Account save(Account account) {
        return accountDao.save(account);
    }

    public Optional<Account> findByName(String name) {
        return accountDao.findByName(name);
    }

    public void deleteById(int id) {
        accountDao.deleteById(id);
    }

    private Account convertDtoToAccount(AccountDto dto) {

        Account result = accountDao.findById(dto.getAccountId()).orElseGet(Account::new);

        result.setName(dto.getName());
        result.setTps(dto.getTps());
        result.setSmscAccount(smscAccountService.getById(dto.getSmscAccountId()));

        return result;
    }

    public String getNameById(Integer accountId) throws AccountException {
        if (accountId == null) {
            throw new AccountException("accountId is null");
        }
        return accountDao.findNameById(accountId);
    }
}
