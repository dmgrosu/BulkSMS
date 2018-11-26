package com.emotion.ecm.dao;

import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.SmppAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SmppAddressDao extends JpaRepository<SmppAddress, Integer> {

    List<SmppAddress> findAllByAccount(Account account);

}
