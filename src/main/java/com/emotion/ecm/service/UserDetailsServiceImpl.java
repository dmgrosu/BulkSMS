package com.emotion.ecm.service;

import com.emotion.ecm.dao.AppRoleDao;
import com.emotion.ecm.dao.AppUserDao;
import com.emotion.ecm.model.AppRole;
import com.emotion.ecm.model.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private AppUserDao appUserDao;
    private AppRoleDao appRoleDao;

    @Autowired
    public UserDetailsServiceImpl(AppUserDao appUserDao, AppRoleDao appRoleDao) {
        this.appUserDao = appUserDao;
        this.appRoleDao = appRoleDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<AppUser> optionalAppUser = appUserDao.findByUsername(username);
        if (!optionalAppUser.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }

        AppUser appUser = optionalAppUser.get();
        return new User(appUser.getUsername(), appUser.getPassword(), getGrantedAuthorities(appUser));

    }

    private List<GrantedAuthority> getGrantedAuthorities(AppUser user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        List<AppRole> roles = appRoleDao.findAllByUser(user);
        for (AppRole role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        }
        return authorities;
    }

}
