package com.BookShop_Backend.DTO.User;

import com.BookShop_Backend.models.UserEntity;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;


@Getter
public class MyUserDetail implements UserDetails {

    private final Long id;
    private final String fullName;
    private final String phoneNumber;
    private final String password;
    private String address;
    private Date dateOfBirth;
    private final Collection<? extends GrantedAuthority> authorities;

    public MyUserDetail(UserEntity user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.phoneNumber = user.getPhoneNumber();
        this.password = user.getPassword();
        this.address = user.getAddress();
        this.dateOfBirth = user.getDateOfBirth();

        this.authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().getName())
        );
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
