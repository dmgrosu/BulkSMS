package com.emotion.ecm.dao;

import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.SmppAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SmppAddressDao extends JpaRepository<SmppAddress, Integer> {

    List<SmppAddress> findAllByAccount(Account account);

    Optional<SmppAddress> findByAccountAndAddress(Account account, String address);

}
