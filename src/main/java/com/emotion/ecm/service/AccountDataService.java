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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    public Optional<AccountData> getById(int accountDataId) {
        return accountDataDao.findById(accountDataId);
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

    public List<String> getNumbersFromFile(int accountDataId) throws IOException {

        List<String> result = new ArrayList<>();

        try {
            Optional<AccountData> optional = getById(accountDataId);
            if (optional.isPresent()) {
                AccountData accountData = optional.get();
                Path directory = getAccountPath(accountData.getUser());
                Path fullPath = directory.resolve(accountData.getFileName());
                BufferedReader reader = Files.newBufferedReader(fullPath);
                String currLine;
                while ((currLine = reader.readLine()) != null) {
                    result.add(currLine);
                }
                reader.close();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new IOException();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }

        return result;
    }

    public int getNumbersCountFromFile(int accountDataId) throws IOException {

        int result = 0;

        try {
            Optional<AccountData> optional = getById(accountDataId);
            if (optional.isPresent()) {
                AccountData accountData = optional.get();
                Path directory = getAccountPath(accountData.getUser());
                Path fullPath = directory.resolve(accountData.getFileName());
                BufferedReader reader = Files.newBufferedReader(fullPath);
                while (reader.readLine() != null) {
                    result++;
                }
                reader.close();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new IOException();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }

        return result;
    }

    public void deleteById(int accountDataId) {
        accountDataDao.deleteById(accountDataId);
    }

    private AccountData convertDtoToAccountData(AccountDataDto accountDataDto, AppUser user) {
        AccountData result = accountDataDao.findById(accountDataDto.getAccountDataId()).orElseGet(AccountData::new);
        result.setFileName(accountDataDto.getFile().getOriginalFilename());
        result.setName(accountDataDto.getName());
        result.setUser(user);
        result.setLinesCount(accountDataDto.getLinesCount());
        return result;
    }

}
