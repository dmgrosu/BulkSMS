package com.emotion.ecm.service;

import com.emotion.ecm.dao.AccountDataDao;
import com.emotion.ecm.model.AccountData;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.dto.AccountDataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class AccountDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDataService.class);

    private AccountDataDao accountDataDao;

    @Autowired
    public AccountDataService(AccountDataDao accountDataDao) {
        this.accountDataDao = accountDataDao;
    }

    public List<AccountData> getAllByUser(AppUser user) {
        return accountDataDao.findAllByUser(user);
    }

    public Optional<AccountData> getByNameAndUser(AppUser user, String name) {
        return accountDataDao.findByNameAndUser(name, user);
    }

    public Optional<AccountData> getByNameAndFileNameAndUser(String name, String fileName, AppUser user) {
        return accountDataDao.findByNameAndFileNameAndUser(name, fileName, user);
    }

    public Path getAccountPath(AppUser user) throws IOException {

        String currAccountName = user.getAccount().getName();
        String directory = System.getProperty("catalina.base") + File.separator +
                "logs" + File.separator + "ecmData" + File.separator;
        Path result = Paths.get(directory + currAccountName);
        if (!Files.exists(result)) {
            Files.createDirectories(result);
        }

        return result;
    }

    @Transactional
    public void saveNewAccountData(AccountDataDto accountDataDto, AppUser user) {
        accountDataDao.save(convertDtoToAccountData(accountDataDto, user));
        LOGGER.info(String.format("%s: new account data saved", user.getUsername()));
    }

    private AccountData convertDtoToAccountData(AccountDataDto accountDataDto, AppUser user) {
        AccountData result = new AccountData();
        result.setFileName(accountDataDto.getFile().getOriginalFilename());
        result.setName(accountDataDto.getName());
        result.setUser(user);
        return result;
    }

}
