package com.emotion.ecm.dao;

import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupDao extends JpaRepository<Group, Integer> {

    List<Group> findAllByUser(AppUser user);

}
