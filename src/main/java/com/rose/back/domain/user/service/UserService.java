package com.rose.back.domain.user.service;

import java.util.List;

import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.domain.user.dto.request.MemberSearchCondition;

public interface UserService {
    
    public UserInfoDto get(String userId);
    
    public boolean validatePassword(String userId, String userPwd);
    
    public UserInfoDto updateUser(UserInfoDto request);

    public void modify(UserInfoDto userDto);

    public List<MemberSearchCondition> findAll();

    public boolean isAdmin(String userId);
}
