package com.rose.back.domain.board.service;

import com.rose.back.domain.board.entity.ImageEntity;
import com.rose.back.domain.board.repository.ImageRepository;
import com.rose.back.domain.board.util.ImageUploadHelper;
import com.rose.back.infra.S3.S3Uploader;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ImageUploadHelper imageUploadHelper;
    private final S3Uploader s3Uploader;

    @Value("${img.upload-dir}")
    private String imgDir;

    public String uploadBoardImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("파일이 없습니다.");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            throw new IOException("파일명 오류: 확장자가 없습니다.");
        }
        String ext = fileName.substring(fileName.lastIndexOf("."));
        String uuidFileName = UUID.randomUUID() + ext;

        Path uploadDir = Paths.get(imgDir).toAbsolutePath();
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path filePath = uploadDir.resolve(uuidFileName);
        file.transferTo(filePath.toFile());
        try {
            String s3Url = s3Uploader.uploadFile(filePath);
            Files.delete(filePath);
            return s3Url;
        } catch (Exception e) {
            log.error("S3 업로드 실패", e); // 전체 스택 출력
            throw new IOException("S3 업로드 실패: " + e.getMessage());
        }
    }

    public List<ImageEntity> saveImagesForBoard(ImageEntity imageEntity, List<MultipartFile> files) throws Exception {
        // 파일을 저장하고 그 Board 에 대한 list 를 가지고 있는다
        List<ImageEntity> list = imageUploadHelper.parseFileInfo(imageEntity.getId(), files);

        if (list.isEmpty()){
            return new ArrayList<>(); // 파일 없을 경우 빈 리스트 반환
        } else { // 파일에 대해 DB에 저장하고 가지고 있을 것
            List<ImageEntity> pictureBeans = new ArrayList<>();
            for (ImageEntity boards : list) {
                pictureBeans.add(imageRepository.save(boards));
            }
            return pictureBeans; // 저장한 목록 반환
        }
    }

    public List<ImageEntity> findBoards() {
        return imageRepository.findAll();
    }

    public Optional<ImageEntity> findBoard(Long id) {
        return imageRepository.findById(id);
    }
}