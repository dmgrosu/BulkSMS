package com.emotion.ecm.service;

import com.emotion.ecm.dao.AppUserDao;
import com.emotion.ecm.model.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private AppUserDao appUserDao;

    @Autowired
    public UserDetailsServiceImpl(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
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
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

}
