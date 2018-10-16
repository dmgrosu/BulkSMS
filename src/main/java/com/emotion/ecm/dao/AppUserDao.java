package com.emotion.ecm.dao;

import com.emotion.ecm.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserDao extends JpaRepository<AppUser, Integer> {

    Optional<AppUser> findByUsername(String username);

}
