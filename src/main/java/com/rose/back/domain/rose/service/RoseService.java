package com.rose.back.domain.rose.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.rose.back.domain.rose.entity.RoseEntity;
import com.rose.back.domain.rose.repository.RoseRepository;
import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.repository.WikiRepository;

@Service
public class RoseService {

    private final RoseRepository userRoseRepository;
    private final WikiRepository roseWikiRepository;

    public RoseService(RoseRepository userRoseRepository, WikiRepository roseWikiRepository) {
        this.userRoseRepository = userRoseRepository;
        this.roseWikiRepository = roseWikiRepository;
    }

    public RoseEntity createUserRose(Long userId, Long wikiId, String nickname, LocalDate acquiredDate, String locationNote) {
        WikiEntity roseWiki = roseWikiRepository.findById(wikiId)
            .orElseThrow(() -> new RuntimeException("도감 품종이 존재하지 않습니다"));

        RoseEntity userRose = new RoseEntity();
        userRose.setWikiEntity(roseWiki);
        userRose.setUserId(userId);
        userRose.setNickname(nickname);
        userRose.setAcquiredDate(acquiredDate);
        userRose.setLocationNote(locationNote);

        return userRoseRepository.save(userRose);
    }
}
