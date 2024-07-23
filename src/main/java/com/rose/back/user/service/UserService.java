package com.rose.back.user.service;

import com.rose.back.user.dto.UserDTO;

public interface UserService {
    
    public UserDTO get(String userId);
    
    public boolean validatePassword(String userId, String userPwd);
    
    public UserDTO updateUser(UserDTO request);

    public void modify(UserDTO userDto);
}
