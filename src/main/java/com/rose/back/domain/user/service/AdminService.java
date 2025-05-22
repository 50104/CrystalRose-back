package com.rose.back.domain.user.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rose.back.domain.user.dto.AdminResponse;
import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.repository.WikiRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final WikiRepository wikiRepository;

    public List<AdminResponse> getPendingList() {
        return wikiRepository.findAllByStatus(WikiEntity.Status.PENDING)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void approve(Long id) {
        WikiEntity wiki = getWikiOrThrow(id);
        wiki.setStatus(WikiEntity.Status.APPROVED);
    }

    public void reject(Long id) {
        WikiEntity wiki = getWikiOrThrow(id);
        wiki.setStatus(WikiEntity.Status.REJECTED);
    }

    private WikiEntity getWikiOrThrow(Long id) {
        return wikiRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("도감을 찾을 수 없습니다. ID = " + id));
    }

    private AdminResponse toDto(WikiEntity wiki) {
        return AdminResponse.builder()
                .id(wiki.getId())
                .name(wiki.getName())
                .category(wiki.getCategory())
                .status(wiki.getStatus().name())
                .createdDate(wiki.getCreatedDate())
                .build();
    }
}