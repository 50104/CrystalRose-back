package com.rose.back.domain.auth.oauth2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.rose.back.domain.user.dto.UserInfoDto;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails  implements UserDetails {

    private final UserInfoDto userDTO;

    public CustomUserDetails(UserInfoDto userDTO) {
        this.userDTO = userDTO;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userDTO.getUserRole();
            }
        });
        return collection;
    }

    public Long getUserNo() {
        return userDTO.getUserNo();
    }

    @Override
    public String getPassword() {
        return userDTO.getUserPwd();
    }

    @Override
    public String getUsername() {
        return userDTO.getUserName();
    }

    public String getUserNick() {
        return userDTO.getUserNick();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
