package com.example.demo.security;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.repo.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findUserByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("No such user");
        }
        UserDetails userDetails=org.springframework.security.core.userdetails.User.builder().username(user.getUsername())
                .password(user.getPassword())
                .authorities(convertRoles(user.getRoles())).build();


        return userDetails;
    }

    private Collection<? extends GrantedAuthority> convertRoles(Collection<Role> roles) {
        List<SimpleGrantedAuthority> listRoles = roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
        List<SimpleGrantedAuthority> listAuthorities = roles.stream().flatMap(role -> role.getAuthorities().stream()).map(role -> new SimpleGrantedAuthority(role.getName())).toList();
        listRoles.addAll(listAuthorities);
        System.out.println(listRoles);
        return listRoles;
    }
}
