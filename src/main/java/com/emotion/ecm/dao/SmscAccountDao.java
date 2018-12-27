package com.emotion.ecm.dao;

import com.emotion.ecm.model.SmscAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmscAccountDao  extends JpaRepository<SmscAccount, Integer> {

    Optional<SmscAccount> findByIpAddressAndPort(String ip, int port);

}
