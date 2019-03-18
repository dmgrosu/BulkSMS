package com.emotion.ecm.dao;

import com.emotion.ecm.model.BlackListMsisdn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BlackListMsisdnDao extends JpaRepository<BlackListMsisdn, Integer> {

    @Query("select msisdn from BlackListMsisdn where blackList.id = ?1")
    List<String> findAllMsisdnByBlackListId(int blackListId);

    long countByBlackListId(int blackListId);

    @Modifying
    @Query("delete from BlackListMsisdn where blackList.id = ?1")
    void deleteAllByBlackListId(int blackListId);
}
