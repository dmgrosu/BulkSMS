package com.emotion.ecm.dao;

import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.ExpirationTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExpirationTimeDao extends JpaRepository<ExpirationTime, Integer> {

    List<ExpirationTime> findAllByAccount(Account account);

    Optional<ExpirationTime> findByAccountIdAndName(int accountId, String name);

    @Query("select value from ExpirationTime where id = ?1")
    String findValueById(int id);

}
