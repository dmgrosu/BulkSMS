package com.emotion.ecm.dao;

import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.SmsPrefixGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SmsPrefixGroupDao extends JpaRepository<SmsPrefixGroup, Integer> {

    Optional<SmsPrefixGroup> findByAccountAndName(Account account, String name);

    List<SmsPrefixGroup> findAllByAccount(Account account);

}
