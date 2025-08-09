package com.rose.back.domain.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.rose.back.domain.board.dto.ContentRequestDto;
import com.rose.back.domain.board.dto.ContentWithWriterDto;
import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.repository.ContentImageRepository;
import com.rose.back.domain.board.repository.ContentRepository;
import com.rose.back.domain.board.repository.RecommendationRepository;
import com.rose.back.domain.comment.repository.CommentRepository;
import com.rose.back.domain.report.repository.UserBlockRepository;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;
import com.rose.back.domain.user.service.UserService;
import com.rose.back.infra.S3.ImageUrlExtractor;
import com.rose.back.infra.S3.S3Uploader;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock private ContentRepository contentRepository;
    @Mock private UserBlockRepository userBlockRepository;
    @Mock private ContentImageService contentImageService;
    @Mock private ContentImageRepository contentImageRepository;
    @Mock private ImageUrlExtractor imageUrlExtractor;
    @Mock private S3Uploader s3Uploader;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;
    @Mock private CommentRepository commentRepository;
    @Mock private RedisViewService redisViewService;
    @Mock private RecommendationRepository recommendationRepository;

    @InjectMocks private ContentService contentService;

    @DisplayName("게시글 저장 성공")
    @Test
    void saveContent_success() {
        // given
        ContentRequestDto req = new ContentRequestDto("제목", "내용", "공지", "user1", "이미지");
        UserEntity user = new UserEntity();
        user.setUserId("user1");

        when(userRepository.findByUserId("user1")).thenReturn(user);
        when(userService.isAdmin("user1")).thenReturn(true);
        when(contentRepository.save(any(ContentEntity.class))).thenAnswer(invocation -> {
            ContentEntity entity = invocation.getArgument(0);
            entity.setBoardNo(1L);
            return entity;
        });
        when(imageUrlExtractor.extractImageUrls(any())).thenReturn(List.of());

        // when
        Long result = contentService.saveContent(req);

        // then
        assertThat(result).isEqualTo(1L);
        verify(contentRepository, times(1)).save(any(ContentEntity.class));
        verify(contentImageService, times(1)).saveImages(any(), any());
    }

    @DisplayName("공지 작성 권한이 없으면 예외 발생")
    @Test
    void saveContent_accessDeniedForNotice() {
        // given
        ContentRequestDto req = new ContentRequestDto("제목", "내용", "공지", "user1", "이미지");
        UserEntity user = new UserEntity();
        user.setUserId("user1");

        when(userRepository.findByUserId("user1")).thenReturn(user);
        when(userService.isAdmin("user1")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> contentService.saveContent(req))
                .isInstanceOf(AccessDeniedException.class);
    }

    @DisplayName("게시글 조회 시 존재하지 않으면 예외 발생")
    @Test
    void selectOneContentDto_notFound() {
        // given
        when(contentRepository.findByBoardNo(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> contentService.selectOneContentDto(1L, null, false))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("게시글 조회 시 좋아요 여부와 댓글 수 포함 반환")
    @Test
    void selectOneContentDto_success() {
        // given
        ContentEntity entity = new ContentEntity();
        entity.setBoardNo(1L);
        UserEntity writer = new UserEntity();
        writer.setUserId("user1");
        writer.setUserStatus(UserEntity.UserStatus.ACTIVE);
        entity.setWriter(writer);

        when(contentRepository.findByBoardNo(1L)).thenReturn(Optional.of(entity));
        when(commentRepository.countByContentEntity_BoardNo(1L)).thenReturn(3L);
        when(recommendationRepository.countByBoardNo(1L)).thenReturn(5L);
        when(recommendationRepository.findByBoardNoAndUserId(1L, "user2"))
                .thenReturn(Optional.ofNullable(null));

        // when
        ContentWithWriterDto dto =
                contentService.selectOneContentDto(1L, "user2", false);

        // then
        assertThat(dto.commentCount()).isEqualTo(3L);
        assertThat(dto.likeCount()).isEqualTo(5L);
        assertThat(dto.recommended()).isFalse();
    }
}
