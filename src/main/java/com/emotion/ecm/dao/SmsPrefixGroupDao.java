package com.emotion.ecm.dao;

import com.emotion.ecm.exception.PrefixException;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.SmsPrefixGroup;
import com.emotion.ecm.model.dto.PrefixGroupDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SmsPrefixGroupDao extends JpaRepository<SmsPrefixGroup, Integer> {

    Optional<SmsPrefixGroup> findByAccountAndName(Account account, String name);

    List<SmsPrefixGroup> findAllByAccount(Account account);

    @Query("select new com.emotion.ecm.model.dto.PrefixGroupDto " +
            "(pg.id, pg.name, pg.account.id) " +
            "from SmsPrefixGroup pg " +
            "where id = ?1")
    PrefixGroupDto findDtoById(int groupId) throws PrefixException;
}
