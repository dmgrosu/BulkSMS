package com.emotion.ecm.dao;

import com.emotion.ecm.exception.SmppAddressException;
import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.SmppAddress;
import com.emotion.ecm.model.dto.SmppAddressDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SmppAddressDao extends JpaRepository<SmppAddress, Integer> {

    @Query("select new com.emotion.ecm.model.dto.SmppAddressDto" +
            "(a.id, a.address) " +
            "from SmppAddress a " +
            "where a.account = ?1")
    List<SmppAddressDto> findAllAddressesByAccount(Account account);

    Optional<SmppAddress> findByAccountAndAddress(Account account, String address);

    @Query("select new com.emotion.ecm.model.dto.SmppAddressDto" +
            "(a.id, a.address, a.ton, a.npi, a.account.id) " +
            "from SmppAddress a " +
            "where a.account = ?1")
    List<SmppAddressDto> findAllDtoByAccount(Account account);

    @Query("select new com.emotion.ecm.model.dto.SmppAddressDto" +
            "(a.id, a.address, a.ton, a.npi, a.account.id) " +
            "from SmppAddress a " +
            "where a.id = ?1")
    SmppAddressDto findDtoById(int id) throws SmppAddressException;

}
