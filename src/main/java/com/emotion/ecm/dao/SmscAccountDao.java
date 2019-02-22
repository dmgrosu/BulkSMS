package com.emotion.ecm.dao;

import com.emotion.ecm.exception.SmscAccountException;
import com.emotion.ecm.model.SmscAccount;
import com.emotion.ecm.model.dto.SmscAccountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SmscAccountDao  extends JpaRepository<SmscAccount, Integer> {

    Optional<SmscAccount> findByIpAddressAndPort(String ip, int port);

    @Query("select new com.emotion.ecm.model.dto.SmscAccountDto" +
            "(a.id, a.systemId, a.password, a.ipAddress, a.port, a.tps, a.maxConnections, a.asynchronous) " +
            "from SmscAccount a")
    List<SmscAccountDto> findAllDto();

    @Query("select new com.emotion.ecm.model.dto.SmscAccountDto" +
            "(a.id, a.systemId, a.ipAddress) " +
            "from SmscAccount a")
    List<SmscAccountDto> findAllNames();

    @Query("select new com.emotion.ecm.model.dto.SmscAccountDto" +
            "(a.id, a.systemId, a.password, a.ipAddress, a.port, a.tps, a.maxConnections, a.asynchronous) " +
            "from SmscAccount a " +
            "where a.id = ?1")
    SmscAccountDto findDtoById(int id) throws SmscAccountException;

    @Query("select a.tps from SmscAccount a where a.id = ?1")
    Integer findTpsById(int id);

}
