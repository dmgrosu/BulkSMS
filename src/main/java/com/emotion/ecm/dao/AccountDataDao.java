package com.emotion.ecm.dao;

import com.emotion.ecm.model.AccountData;
import com.emotion.ecm.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountDataDao extends JpaRepository<AccountData, Integer> {

    List<AccountData> findAllByUser(AppUser user);

}
