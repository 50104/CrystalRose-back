package com.rose.back.user.service;

import java.util.List;

import com.rose.back.user.dto.UserDTO;
import com.rose.back.user.dto.request.MemberListReqDto;

public interface UserService {
    
    public UserDTO get(String userId);
    
    public boolean validatePassword(String userId, String userPwd);
    
    public UserDTO updateUser(UserDTO request);

    public void modify(UserDTO userDto);

    public List<MemberListReqDto> findAll();
}
