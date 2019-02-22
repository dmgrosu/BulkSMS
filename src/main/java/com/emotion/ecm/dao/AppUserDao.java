package com.emotion.ecm.dao;

import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.dto.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AppUserDao extends JpaRepository<AppUser, Integer> {

    @Query("select u from AppUser u " +
            "join fetch u.roles r " +
            "where u.username = ?1")
    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    @Query(value = "select new com.emotion.ecm.model.dto.UserDto" +
            "(u.id, u.firstName, u.lastName, u.email, u.username, u.status, u.account.id, u.account.name) " +
            "from AppUser u")
    List<UserDto> findAllDto();

    @Query(value = "select new com.emotion.ecm.model.dto.UserDto(u.id, u.account.id) " +
            "from AppUser u where u.account.id in (?1)")
    List<UserDto> findAllDtoByAccountIds(Set<Integer> accountIds);

}
