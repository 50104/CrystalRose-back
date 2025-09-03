package com.rose.back.domain.diary.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Service;

import com.rose.back.domain.diary.dto.CalendarDataResponse;
import com.rose.back.domain.diary.dto.DiaryResponse;
import com.rose.back.domain.diary.dto.RoseCareLogDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CareLogService careLogService;
    private final DiaryService diaryService;

    public CalendarDataResponse getCalendarData(Long userNo, String startDate, String endDate) {
        log.info("캘린더 데이터 조회 시작 - userNo: {}, 기간: {} ~ {}", userNo, startDate, endDate);

        CompletableFuture<List<RoseCareLogDto>> careFuture = CompletableFuture.supplyAsync(
                () -> careLogService.getAllLogsByDateRange(userNo, startDate, endDate)
        );
        CompletableFuture<List<DiaryResponse>> diaryFuture = CompletableFuture.supplyAsync(
                () -> diaryService.getUserTimelineByDateRange(userNo, startDate, endDate)
        );

        long timeoutMillis = 10_000L; // 10초
        List<RoseCareLogDto> careLogs = List.of();
        List<DiaryResponse> diaries = List.of();

        try {
            CompletableFuture.allOf(careFuture, diaryFuture).get(timeoutMillis, TimeUnit.MILLISECONDS);

            if (careFuture.isDone() && !careFuture.isCompletedExceptionally()) {
                careLogs = careFuture.join();
            }
            if (diaryFuture.isDone() && !diaryFuture.isCompletedExceptionally()) {
                diaries = diaryFuture.join();
            }

        } catch (TimeoutException te) {
            log.warn("캘린더 데이터 조회 타임아웃 ({} ~ {})", startDate, endDate, te);
            cancelFutures(careFuture, diaryFuture);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("캘린더 데이터 조회 인터럽트", ie);
            cancelFutures(careFuture, diaryFuture);
        } catch (ExecutionException ee) {
            log.error("캘린더 데이터 조회 실행 예외", ee);
            cancelFutures(careFuture, diaryFuture);
        } finally {
            if (!careFuture.isDone()) careFuture.cancel(true);
            if (!diaryFuture.isDone()) diaryFuture.cancel(true);
        }

        log.info("캘린더 데이터 조회 완료 - 케어로그: {}개, 다이어리: {}개", careLogs.size(), diaries.size());
        return new CalendarDataResponse(careLogs, diaries);
    }

    public CalendarDataResponse getGuestCalendarData() {
        log.info("비로그인 사용자의 캘린더 데이터 조회");
        return new CalendarDataResponse(List.of(), List.of());
    }

    private void cancelFutures(CompletableFuture<?>... futures) {
        for (CompletableFuture<?> future : futures) {
            future.cancel(true);
        }
    }
}
