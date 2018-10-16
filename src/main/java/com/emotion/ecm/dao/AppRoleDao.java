package com.emotion.ecm.dao;

import com.emotion.ecm.model.AppRole;
import com.emotion.ecm.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppRoleDao extends JpaRepository<AppRole, Integer> {

    Optional<AppRole> findByName(String name);

    List<AppRole> findAllByUser(AppUser appUser);

}
