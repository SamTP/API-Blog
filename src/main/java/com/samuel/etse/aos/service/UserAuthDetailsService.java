package com.samuel.etse.aos.service;

import com.samuel.etse.aos.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que proporciona usuarios con sus roles
 */
@Service
public class UserAuthDetailsService implements UserDetailsService {
    private final UserRepository db;

    @Autowired
    UserAuthDetailsService(final UserRepository db){
        this.db = db;
    }

    // Crea un User con GrantedAuthorities
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {

        // Recupera el usuario por el nombre
        final com.samuel.etse.aos.model.User user = db.findByUsername(username);

        if(user == null)
            throw new UsernameNotFoundException(username);

        if(user.isActive()==false)
            throw  new UsernameNotFoundException(username);

        // Recupera los roles y los parsea
        final String roles = user.getRoles()
                .stream()
                .map(role -> String.format("ROLE_%s", role))
                .collect(Collectors.joining(","));

        final List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(roles);

        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
