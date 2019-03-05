package com.emotion.ecm.dao;

import com.emotion.ecm.exception.SmscAccountException;
import com.emotion.ecm.model.SmscAccount;
import com.emotion.ecm.model.dto.SmscAccountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SmscAccountDao extends JpaRepository<SmscAccount, Integer> {

    Optional<SmscAccount> findByIpAddressAndPort(String ip, int port);

    @Query("select new com.emotion.ecm.model.dto.SmscAccountDto" +
            "(id, systemId, password, ipAddress, systemType, port, tps, maxConnections, asynchronous) " +
            "from SmscAccount")
    List<SmscAccountDto> findAllDto();

    @Query("select new com.emotion.ecm.model.dto.SmscAccountDto" +
            "(id, systemId, ipAddress) " +
            "from SmscAccount")
    List<SmscAccountDto> findAllNames();

    @Query("select new com.emotion.ecm.model.dto.SmscAccountDto" +
            "(id, systemId, password, ipAddress, systemType, port, tps, maxConnections, asynchronous) " +
            "from SmscAccount " +
            "where id = ?1")
    SmscAccountDto findDtoById(int id) throws SmscAccountException;

    @Query("select tps from SmscAccount where id = ?1")
    Integer findTpsById(int id);

}
