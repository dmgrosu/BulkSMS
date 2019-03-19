package com.emotion.ecm.dao;

import com.emotion.ecm.model.BlackListMsisdn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface BlackListMsisdnDao extends JpaRepository<BlackListMsisdn, Integer> {

    @Query("select msisdn from BlackListMsisdn where blackList.id = ?1")
    List<String> findAllMsisdnByBlackListId(int blackListId);

    long countByBlackListId(int blackListId);

    @Modifying
    @Query("delete from BlackListMsisdn where blackList.id = ?1")
    void deleteAllByBlackListId(int blackListId);

    @Query("select distinct bl.msisdn from BlackListMsisdn bl " +
            "join bl.blackList b " +
            "left join bl.blackList.account a " +
            "where b.account is null or a.id = ?1")
    Set<String> getBlacklistedMsisdnsByAccount(Integer accountId);

    @Query("select distinct msisdn from BlackListMsisdn")
    Set<String> getAllBlacklistedMsisdn();
}
