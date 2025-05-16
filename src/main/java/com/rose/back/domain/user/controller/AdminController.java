package com.rose.back.domain.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.domain.user.controller.docs.AdminControllerDocs;

import java.util.*;

import lombok.RequiredArgsConstructor;

// @RequestMapping("/")
@RestController
@RequiredArgsConstructor
public class AdminController implements AdminControllerDocs{

    // AdminController
    @GetMapping("/api/v1/admin")
    public String adminP() {

        return "Admin Controller";
    }

    @GetMapping("/")
    public String mainApi() {

        // 세션 현재 사용자 아이디
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        // 세션 현재 사용자 role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();
        GrantedAuthority auth = iter.next();
        String role = auth.getAuthority();

        return "Main Controller : " + name + role;
    }
}
