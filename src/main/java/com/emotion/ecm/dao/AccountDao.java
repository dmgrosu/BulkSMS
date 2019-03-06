package com.emotion.ecm.dao;

import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.dto.AccountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountDao extends JpaRepository<Account, Integer> {

    @Query("select new com.emotion.ecm.model.dto.AccountDto" +
            "(a.id, a.name, a.tps, a.smscAccount.id, a.smscAccount.systemId) " +
            "from Account a")
    List<AccountDto> getAllDto();

    @Query("select new com.emotion.ecm.model.dto.AccountDto(a.id, a.name) " +
            "from Account a")
    List<AccountDto> getAllNames();

    Optional<Account> findByName(String name);

    @Query("select name from Account where id = ?1")
    String findNameById(Integer accountId);
}
