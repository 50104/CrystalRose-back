package com.rose.back.user.service;

import com.rose.back.user.dto.UserDTO;

public interface UserService {
    
    public UserDTO get(String userId);
}
