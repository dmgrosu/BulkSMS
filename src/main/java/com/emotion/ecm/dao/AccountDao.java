package com.emotion.ecm.dao;

import com.emotion.ecm.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountDao extends JpaRepository<Account, Integer> {
}
