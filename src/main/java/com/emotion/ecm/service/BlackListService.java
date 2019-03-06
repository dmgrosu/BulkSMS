package com.emotion.ecm.service;

import com.emotion.ecm.dao.BlackListDao;
import com.emotion.ecm.dao.BlackListMsisdnDao;
import com.emotion.ecm.exception.AccountException;
import com.emotion.ecm.exception.BlackListException;
import com.emotion.ecm.model.BlackList;
import com.emotion.ecm.model.dto.BlackListDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BlackListService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlackListService.class);

    private BlackListDao blackListDao;
    private BlackListMsisdnDao blackListMsisdnDao;
    private AccountService accountService;

    @Autowired
    public BlackListService(BlackListDao blackListDao, BlackListMsisdnDao blackListMsisdnDao,
                            AccountService accountService) {
        this.blackListDao = blackListDao;
        this.blackListMsisdnDao = blackListMsisdnDao;
        this.accountService = accountService;
    }

    public List<BlackListDto> getAllDto() {
        List<BlackListDto> result = blackListDao.findAllDto();
        for (BlackListDto blackListDto : result) {
            if (blackListDto.getAccountId() == null) {
                blackListDto.setAccountName("--global--");
            } else {
                try {
                    blackListDto.setAccountName(accountService.getNameById(blackListDto.getAccountId()));
                } catch (AccountException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
        }
        return result;
    }

    public List<BlackListDto> getAllDtoByAccountId(Integer accountId) throws BlackListException {
        if (accountId == null) {
            throw new BlackListException("accountId is null");
        }
        return blackListDao.findAllDtoByAccountId(accountId);
    }

    public List<String> getAllMsisdnByBlackListId(int blackListId) {
        return blackListMsisdnDao.findAllMsisdnByBlackListId(blackListId);
    }

    public BlackListDto getDtoById(int id) throws BlackListException {
        if (id == 0) {
            throw new BlackListException(String.format("BlackList with id %s not found", id));
        }
        BlackListDto result = blackListDao.findDtoById(id);
        if (result == null) {
            throw new BlackListException(String.format("BlackList with id %s not found", id));
        }
        return result;
    }

    public Optional<BlackList> findByName(String name) {
        return blackListDao.findByName(name);
    }

    public void save(BlackListDto dto) throws AccountException {
        blackListDao.save(convertDtoToBlackList(dto));
    }

    private BlackList convertDtoToBlackList(BlackListDto dto) throws AccountException {
        BlackList result = blackListDao.findById(dto.getBlackListId()).orElseGet(BlackList::new);
        result.setName(dto.getName());
        if (dto.getAccountId() != null) {
            result.setAccount(accountService.getById(dto.getAccountId()));
        } else {
            result.setAccount(null);
        }
        return result;
    }

    public void deleteById(int id) {
        blackListDao.deleteById(id);
    }
}
