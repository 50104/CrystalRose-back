package com.rose.back.domain.wiki.service;

import java.io.IOException;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.common.util.ImageValidator;
import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.entity.WikiImageEntity;
import com.rose.back.domain.wiki.repository.WikiImageRepository;
import com.rose.back.infra.S3.ImageTempEntity;
import com.rose.back.infra.S3.ImageTempRepository;
import com.rose.back.infra.S3.S3PresignedService;
import com.rose.back.infra.S3.S3Uploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WikiImageService {

    private final S3PresignedService s3PresignedService;
    private final S3Uploader s3Uploader;
    private final ImageTempRepository tempRepository;
    private final WikiImageRepository wikiImageRepository;

    public String uploadImage(MultipartFile file, String uploader) throws IOException {
        ImageValidator.validate(file);
        String url = s3Uploader.uploadFile("wikis", file);
        tempRepository.save(ImageTempEntity.builder()
            .fileUrl(url)
            .domainType(ImageTempEntity.DomainType.WIKI)
            .uploadedAt(new Date())
            .uploadedBy(uploader)
            .build());
        return url;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, null);
    }

    @Transactional
    public void saveAndBindImage(String fileUrl, WikiEntity wiki) {
        if (!wikiImageRepository.existsByFileUrl(fileUrl)) {
            String s3Key = s3PresignedService.extractKeyFromUrl(fileUrl);
            
            wikiImageRepository.save(WikiImageEntity.builder()
                .fileUrl(fileUrl)
                .s3Key(s3Key)
                .storedFileName(fileUrl.replace("https://dodorose.com/", ""))
                .originalFileName(null) 
                .wiki(wiki)
                .build());
            log.info("WikiImageEntity 저장 완료: url={}, s3Key={}", fileUrl, s3Key);
        }
        tempRepository.findByFileUrl(fileUrl).ifPresent(tempRepository::delete);
        wiki.setImageUrl(fileUrl);
    }

    private List<String> parseImageInput(Object imageInput) {
        if (imageInput == null) return List.of();
        if (imageInput instanceof String s) {
            return List.of(s);
        }
        if (imageInput instanceof String[] arr) {
            return Arrays.stream(arr).filter(Objects::nonNull).toList();
        }

        if (imageInput instanceof Collection<?> coll) {
            return coll.stream()
                      .filter(Objects::nonNull)
                      .filter(item -> item instanceof String)
                      .map(item -> (String) item)
                      .toList();
        }
        throw new IllegalArgumentException("지원되지 않는 이미지 입력 타입입니다: " + imageInput.getClass());
    }

    @Transactional
    public void wikiModification(Object imageInput, WikiEntity wiki) {
        List<String> newFileUrls = parseImageInput(imageInput);
        if (newFileUrls.isEmpty()) return;

        String newFileUrl = newFileUrls.get(0);
        List<WikiImageEntity> existingImages = wikiImageRepository.findByWiki(wiki);

        if (!existingImages.isEmpty()) {
            WikiImageEntity existing = existingImages.get(0);

            if (Objects.equals(existing.getFileUrl(), newFileUrl)) {
                log.info("이미지 변경 없음 - 기존과 동일: {}", newFileUrl);
                tempRepository.findByFileUrl(newFileUrl).ifPresent(tempRepository::delete);
                wiki.setImageUrl(newFileUrl);
                return;
            }

            String oldS3Key = s3PresignedService.extractKeyFromUrl(existing.getFileUrl());
            try {
                s3PresignedService.deleteFile(oldS3Key);
            } catch (Exception e) {
                log.warn("기존 S3 파일 삭제 실패 (key={}): {}", oldS3Key, e.getMessage(), e);
            }

            String newS3Key = s3PresignedService.extractKeyFromUrl(newFileUrl);
            existing.setFileUrl(newFileUrl);
            existing.setS3Key(newS3Key);
            existing.setStoredFileName(newFileUrl.replace("https://dodorose.com/", ""));
            existing.setOriginalFileName(null);
            log.info("기존 WikiImageEntity 덮어쓰기 완료 (id 유지): {}", existing.getId());
        } else {
            String s3Key = s3PresignedService.extractKeyFromUrl(newFileUrl);
            WikiImageEntity saved = wikiImageRepository.save(WikiImageEntity.builder()
                .fileUrl(newFileUrl)
                .s3Key(s3Key)
                .storedFileName(newFileUrl.replace("https://dodorose.com/", ""))
                .originalFileName(null)
                .wiki(wiki)
                .build());
            log.info("새 WikiImageEntity 저장: {}", saved.getId());
        }

        tempRepository.findByFileUrl(newFileUrl).ifPresent(tempRepository::delete);
        wiki.setImageUrl(newFileUrl);
    }

    @Transactional
    public void deleteByWiki(WikiEntity wiki) {
        List<WikiImageEntity> images = wikiImageRepository.findByWiki(wiki);

        for (WikiImageEntity image : images) {
            String s3Key = s3PresignedService.extractKeyFromUrl(image.getFileUrl());
            s3PresignedService.deleteFile(s3Key);
            tempRepository.findByFileUrl(image.getFileUrl()).ifPresent(tempRepository::delete);
            log.info("위키 이미지 삭제 완료: id={}, url={}", image.getId(), image.getFileUrl());
        }

        wikiImageRepository.deleteAll(images);
        wiki.setImageUrl(null);
    }
}