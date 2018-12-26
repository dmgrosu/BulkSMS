package com.emotion.ecm.service;

import com.emotion.ecm.dao.AccountDao;
import com.emotion.ecm.dao.SmsPrefixDao;
import com.emotion.ecm.dao.SmsPrefixGroupDao;
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

    public Optional<SmsPrefix> getByGroupAndPrefix(SmsPrefixGroup group, String prefix) {
        return prefixDao.findByPrefixGroupAndPrefix(group, prefix);
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

    public Optional<SmsPrefixGroup> getGroupById(int groupId) {
        return groupDao.findById(groupId);
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

    public SmsPrefix convertDtoToPrefix(PrefixDto prefixDto) throws IllegalArgumentException {

        SmsPrefix result = new SmsPrefix();

        Optional<SmsPrefixGroup> optionalGroup = groupDao.findById(prefixDto.getGroupId());
        if (!optionalGroup.isPresent()) {
            throw new IllegalArgumentException("no group with id " + prefixDto.getGroupId());
        }

        result.setPrefix(prefixDto.getPrefix());
        result.setPrefixGroup(optionalGroup.get());

        return result;
    }

    @Transactional
    public SmsPrefix createNewPrefix(PrefixDto prefixDto) throws NullPointerException {
        if (prefixDto.getGroupId() == 0) {
            throw new NullPointerException("no groupId in prefixDto");
        }
        return prefixDao.save(convertDtoToPrefix(prefixDto));
    }

    @Transactional
    public SmsPrefix updatePrefix(PrefixDto prefixDto) throws NullPointerException {

        int prefixId = prefixDto.getPrefixId();

        if (prefixId == 0) {
            throw new NullPointerException("no prefixId in prefixDto");
        }
        Optional<SmsPrefix> optional = prefixDao.findById(prefixId);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("no prefix with id " + prefixId + " found");
        }
        SmsPrefix smsPrefix = optional.get();
        if (smsPrefix.getPrefix().equals(prefixDto.getPrefix())) {
            return smsPrefix;
        } else {
            return prefixDao.save(smsPrefix);
        }

    }

    @Transactional
    public void deleteGroup(int groupId) {
        groupDao.deleteById(groupId);
    }

    @Transactional
    public void deletePrefix(int prefixId) {
        prefixDao.deleteById(prefixId);
    }

    @Transactional
    public SmsPrefixGroup createNewGroup(PrefixGroupDto groupDto) {
        return groupDao.save(convertDtoToGroup(groupDto));
    }

    public SmsPrefixGroup convertDtoToGroup(PrefixGroupDto groupDto) throws NullPointerException {

        if (groupDto.getAccountId() == 0) {
            throw new NullPointerException("no account id");
        }
        Optional<Account> optionalAccount = accountDao.findById(groupDto.getAccountId());
        if (!optionalAccount.isPresent()) {
            throw new NullPointerException("no account with id " + groupDto.getAccountId() + " found!");
        }
        SmsPrefixGroup result = new SmsPrefixGroup();
        result.setAccount(optionalAccount.get());
        result.setName(groupDto.getGroupName());

        return result;
    }

    @Transactional
    public SmsPrefixGroup updateGroup(PrefixGroupDto groupDto) {
        if (groupDto.getGroupId() == 0) {
            throw new NullPointerException("no groupId in groupDto");
        }
        Optional<SmsPrefixGroup> optional = groupDao.findById(groupDto.getGroupId());
        if (optional.isPresent()) {
            throw new IllegalArgumentException("no group with id " + groupDto.getGroupId() + " found");
        }
        SmsPrefixGroup group = optional.get();
        if (group.getName().equals(groupDto.getGroupName())) {
            return group;
        } else {
            return groupDao.save(group);
        }
    }

}
