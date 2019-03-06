package com.emotion.ecm.dao;

import com.emotion.ecm.model.BlackList;
import com.emotion.ecm.model.dto.BlackListDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BlackListDao extends JpaRepository<BlackList, Integer> {

    @Query("select new com.emotion.ecm.model.dto.BlackListDto" +
            "(id, name, account.id, account.name) " +
            "from BlackList " +
            "where account.id = ?1")
    List<BlackListDto> findAllDtoByAccountId(Integer accountId);

    @Query("select new com.emotion.ecm.model.dto.BlackListDto" +
            "(id, name, account.id) " +
            "from BlackList")
    List<BlackListDto> findAllDto();

    @Query("select new com.emotion.ecm.model.dto.BlackListDto" +
            "(id, name, account.id) " +
            "from BlackList " +
            "where id = ?1")
    BlackListDto findDtoById(int id);

    Optional<BlackList> findByName(String name);
}
