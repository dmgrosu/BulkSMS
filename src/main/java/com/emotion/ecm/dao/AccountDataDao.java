package com.emotion.ecm.dao;

import com.emotion.ecm.model.AccountData;
import com.emotion.ecm.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountDataDao extends JpaRepository<AccountData, Integer> {

    List<AccountData> findAllByUser(AppUser user);

    Optional<AccountData> findByNameAndUser(String name, AppUser user);

    Optional<AccountData> findByNameAndFileNameAndUser(String name, String fileName, AppUser user);

}
