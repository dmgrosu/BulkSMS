package com.emotion.ecm.dao;

import com.emotion.ecm.exception.ContactException;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.Group;
import com.emotion.ecm.model.dto.ContactGroupDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupDao extends JpaRepository<Group, Integer> {

    List<Group> findAllByUser(AppUser user);

    Optional<Group> findByNameAndUser(String groupName, AppUser user);

    @Query("select new com.emotion.ecm.model.dto.ContactGroupDto" +
            "(g.id, g.name, g.user.id) " +
            "from Group g " +
            "where g.user.id = ?1")
    List<ContactGroupDto> findAllDtoByUserId(int userId);

    @Query("select new com.emotion.ecm.model.dto.ContactGroupDto" +
            "(g.id, g.name) " +
            "from Group g " +
            "where g.id = ?1")
    ContactGroupDto findDtoById(int id) throws ContactException;

}
