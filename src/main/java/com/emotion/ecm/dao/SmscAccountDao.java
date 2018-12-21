package com.emotion.ecm.dao;

import com.emotion.ecm.model.Account;
import com.emotion.ecm.model.SmscAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SmscAccountDao  extends JpaRepository<SmscAccount, Integer> {

    List<SmscAccount> findAllByAccount(Account account);

    Optional<SmscAccount> findByAccountIdAndIpAddressAndPort(int accountId, String ip, int port);

}
