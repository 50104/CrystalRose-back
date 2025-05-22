package com.rose.back.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.domain.user.controller.docs.AdminControllerDocs;
import com.rose.back.domain.user.dto.AdminResponse;
import com.rose.back.domain.user.service.AdminService;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController implements AdminControllerDocs{

    private final AdminService adminService;

    @GetMapping("/wiki/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminResponse> getPendingWikiList() { // 승인 대기 도감 리스트
        return adminService.getPendingList();
    }

    @PatchMapping("/wiki/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveWiki(@PathVariable("id") Long id) { // 도감 승인
        adminService.approve(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/wiki/{id}/reject") 
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectWiki(@PathVariable("id") Long id) { // 도감 거절
        adminService.reject(id);
        return ResponseEntity.ok().build();
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
