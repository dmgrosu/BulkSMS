package com.emotion.ecm.service;

import com.emotion.ecm.dao.AppRoleDao;
import com.emotion.ecm.dao.AppUserDao;
import com.emotion.ecm.enums.RoleName;
import com.emotion.ecm.model.AppRole;
import com.emotion.ecm.model.AppUser;
import com.emotion.ecm.model.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AppUserService {

    private static final Logger LOG = LoggerFactory.getLogger(AppUserService.class);

    private AppUserDao appUserDao;
    private AppRoleDao appRoleDao;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AppUserService(AppUserDao u, AppRoleDao r, PasswordEncoder e) {
        this.appUserDao = u;
        this.appRoleDao = r;
        this.passwordEncoder = e;
    }

    public Optional<AppUser> getByUsername(String username) {
        Optional<AppUser> user = appUserDao.findByUsername(username);
        if (!user.isPresent()) {
            LOG.debug(String.format("User %s not found", username));
        }
        return user;
    }

    public Optional<AppUser> getByEmail(String email) {
        Optional<AppUser> user = appUserDao.findByEmail(email);
        if (!user.isPresent()) {
            LOG.warn(String.format("User with e-mail %s not found", email));
        }
        return user;
    }

    public void registerNewUser(UserDto userDto) {

        AppUser appUser = convertUserDtoToUser(userDto);
        appUser.setPassword(passwordEncoder.encode(userDto.getPassword()));

        Optional<AppRole> optionalRole = appRoleDao.findByName(RoleName.USER);
        AppRole roleUser = optionalRole.orElseGet(this::createNewUserRole);
        Set<AppRole> roles = new HashSet<>();
        roles.add(roleUser);

        appUser.setRoles(roles);

        appUserDao.save(appUser);

        LOG.info(String.format("User with username %s saved successfully", userDto.getUsername()));
    }

    public AppUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getByUsername(authentication.getName()).orElse(new AppUser());
    }

    private AppUser convertUserDtoToUser(UserDto userDto) {
        AppUser result = new AppUser();
        result.setFirstName(userDto.getFirstName());
        result.setLastName(userDto.getLastName());
        result.setEmail(userDto.getEmail());
        result.setUsername(userDto.getUsername());
        return result;
    }

    private AppRole createNewUserRole() {
        AppRole role = new AppRole();
        role.setName(RoleName.USER);
        return appRoleDao.save(role);
    }
}
