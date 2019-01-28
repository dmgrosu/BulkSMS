package com.emotion.ecm.service;

import com.emotion.ecm.dao.AccountDao;
import com.emotion.ecm.dao.SmsPrefixDao;
import com.emotion.ecm.dao.SmsPrefixGroupDao;
import com.emotion.ecm.exception.AccountException;
import com.emotion.ecm.exception.PrefixException;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.SmsPrefix;
import com.emotion.ecm.model.SmsPrefixGroup;
import com.emotion.ecm.model.dto.PrefixDto;
import com.emotion.ecm.model.dto.PrefixGroupDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SmsPrefixService {

    private AccountDao accountDao;
    private SmsPrefixDao prefixDao;
    private SmsPrefixGroupDao groupDao;

    @Autowired
    public SmsPrefixService(AccountDao accountDao, SmsPrefixDao prefixDao,
                            SmsPrefixGroupDao groupDao) {
        this.accountDao = accountDao;
        this.prefixDao = prefixDao;
        this.groupDao = groupDao;
    }

    public Optional<SmsPrefix> getByGroupIdAndPrefix(int groupId, String prefix) {
        return prefixDao.findByPrefixGroupIdAndPrefix(groupId, prefix);
    }

    public List<SmsPrefix> getAllByPrefixGroup(SmsPrefixGroup group) {
        return prefixDao.findAllByPrefixGroup(group);
    }

    public List<SmsPrefixGroup> getAllGroupsByAccount(Account account) {
        return groupDao.findAllByAccount(account);
    }

    public Optional<SmsPrefixGroup> getGroupByAccountAndName(Account account, String name) {
        return groupDao.findByAccountAndName(account, name);
    }

    public PrefixGroupDto getGroupDtoById(int groupId) throws PrefixException {
        return groupDao.findDtoById(groupId);
    }

    public boolean isMsisdnValidForAccount(Account account, String msisdn) {
        if (account == null || msisdn == null) {
            return false;
        }
        for (SmsPrefix smsPrefix : getAllPrefixesByAccount(account)) {
            if (msisdn.startsWith(smsPrefix.getPrefix())) {
                return true;
            }
        }
        return false;
    }

    public List<SmsPrefix> getAllPrefixesByAccount(Account account) {

        List<SmsPrefix> result = new ArrayList<>();

        for (SmsPrefixGroup group : getAllGroupsByAccount(account)) {
            result.addAll(getAllByPrefixGroup(group));
        }

        return result;
    }

    public List<PrefixGroupDto> getGroupDtoList(final Account account) {

        return getAllGroupsByAccount(account).stream()
                .map(group -> new PrefixGroupDto(group.getId(), account.getId(), group.getName(),
                        prefixDao.findAllByPrefixGroup(group)))
                .collect(Collectors.toList());
    }


    @Transactional
    public SmsPrefix savePrefix(SmsPrefix prefix) {
        return prefixDao.save(prefix);
    }

    @Transactional
    public SmsPrefix savePrefix(PrefixDto prefixDto) throws PrefixException {
        SmsPrefix prefix = convertPrefixDtoToPrefix(prefixDto);
        return savePrefix(prefix);
    }

    @Transactional
    public SmsPrefixGroup savePrefixGroup(SmsPrefixGroup group) {
        return groupDao.save(group);
    }

    @Transactional
    public SmsPrefixGroup savePrefixGroup(PrefixGroupDto prefixGroupDto, Account currAccount) throws AccountException {
        SmsPrefixGroup group = convertGroupDtoToGroup(prefixGroupDto, currAccount);
        return savePrefixGroup(group);
    }

    @Transactional
    public void deleteGroup(int groupId) {
        groupDao.deleteById(groupId);
    }

    @Transactional
    public void deletePrefix(int prefixId) {
        prefixDao.deleteById(prefixId);
    }

    public PrefixDto getPrefixDtoById(int id) throws PrefixException {
        return prefixDao.findDtoById(id);
    }

    private SmsPrefix convertPrefixDtoToPrefix(PrefixDto prefixDto) throws PrefixException {

        SmsPrefix result = prefixDao.findById(prefixDto.getPrefixId()).orElseGet(SmsPrefix::new);

        if (prefixDto.getPrefixId() == 0) {
            Optional<SmsPrefixGroup> optionalGroup = groupDao.findById(prefixDto.getGroupId());
            if (!optionalGroup.isPresent()) {
                throw new PrefixException("no prefix group with id " + prefixDto.getGroupId());
            }
            if (!optionalGroup.get().equals(result.getPrefixGroup())) {
                result.setPrefixGroup(optionalGroup.get());
            }
        }

        if (!prefixDto.getPrefix().equals(result.getPrefix())) {
            result.setPrefix(prefixDto.getPrefix());
        }

        return result;
    }

    private SmsPrefixGroup convertGroupDtoToGroup(PrefixGroupDto groupDto, Account currAccount) throws AccountException {

        if (currAccount == null) {
            Optional<Account> optionalAccount = accountDao.findById(groupDto.getAccountId());
            if (!optionalAccount.isPresent()) {
                throw new AccountException("no account with id " + groupDto.getAccountId() + " found!");
            }
            currAccount = optionalAccount.get();
        }

        SmsPrefixGroup result = groupDao.findById(groupDto.getGroupId()).orElseGet(SmsPrefixGroup::new);
        if (!groupDto.getGroupName().equals(result.getName())) {
            result.setName(groupDto.getGroupName());
        }
        if (!currAccount.equals(result.getAccount())) {
            result.setAccount(currAccount);
        }

        return result;
    }

}
