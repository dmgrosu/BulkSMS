package com.emotion.ecm.dao;

import com.emotion.ecm.enums.RoleName;
import com.emotion.ecm.model.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppRoleDao extends JpaRepository<AppRole, Integer> {

    Optional<AppRole> findByName(RoleName name);

}
