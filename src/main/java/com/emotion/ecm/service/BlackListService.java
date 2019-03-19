package com.emotion.ecm.service;

import com.emotion.ecm.dao.BlackListDao;
import com.emotion.ecm.dao.BlackListMsisdnDao;
import com.emotion.ecm.exception.AccountException;
import com.emotion.ecm.exception.BlackListException;
import com.emotion.ecm.model.BlackList;
import com.emotion.ecm.model.BlackListMsisdn;
import com.emotion.ecm.model.dto.BlackListDto;
import com.emotion.ecm.model.dto.BlackListMsisdnDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
            blackListDto.setNumbersCount(blackListMsisdnDao.countByBlackListId(blackListDto.getBlackListId()));
        }
        return result;
    }

    /**
     * Returns all blacklisted msisdn(dest numbers): global and by account
     * @param accountId - accountId, nullable
     * @return if accountId is null returns all existing black list msisdn,
     *         otherwise - all 'global' (account is null) black lists
     *         and black lists by selected account
     */
    Set<String> getBlacklistedMsisdn(Integer accountId) {
        if (accountId == null) {
            return blackListMsisdnDao.getAllBlacklistedMsisdn();
        } else {
            return blackListMsisdnDao.getBlacklistedMsisdnsByAccount(accountId);
        }
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

    @Transactional
    public BlackList saveBlackList(BlackList blackList) {
        return blackListDao.save(blackList);
    }

    public void saveBlackList(BlackListDto dto) throws AccountException {
        saveBlackList(convertDtoToBlackList(dto));
    }

    public void deleteBlackListById(int id) {
        deleteMsisdnListByBlackListId(id);
        blackListDao.deleteById(id);
    }

    @Transactional
    public void saveMsisdnList(BlackListMsisdnDto dto) throws BlackListException {
        if (dto == null) {
            throw new BlackListException("blackListMsisdnDto is null");
        }
        Integer blackListId = dto.getBlackListId();
        Set<String> msisdnSet = dto.getMsisdns();
        if (blackListId == null) {
            LOGGER.error("attempt to save msisdn list failed: blackListId is null");
            throw new BlackListException("blackListId is null");
        }
        if (msisdnSet == null) {
            LOGGER.error("attempt to save msisdn list failed: msisdn list in dto is null");
            throw new BlackListException("msisdn list is null");
        }
        Optional<BlackList> optional = blackListDao.findById(blackListId);
        if (!optional.isPresent()) {
            LOGGER.error(String.format("attempt to save msisdn list failed: blackList[%s] not found", blackListId));
            throw new BlackListException(String.format("blackList[%s] not found", blackListId));
        }

        BlackList blackList = optional.get();

        List<BlackListMsisdn> msisdnList = msisdnSet.stream()
                .map(msisdn -> new BlackListMsisdn(msisdn, blackList))
                .collect(Collectors.toList());

        updateMsisdnListByBlackListId(blackList.getId(), msisdnList);
    }

    @Transactional
    public void updateMsisdnListByBlackListId(int blackListId, List<BlackListMsisdn> msisdnList) {
        deleteMsisdnListByBlackListId(blackListId);
        blackListMsisdnDao.saveAll(msisdnList);
    }

    @Transactional
    public void deleteMsisdnListByBlackListId(int blackListId) {
        blackListMsisdnDao.deleteAllByBlackListId(blackListId);
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

}
