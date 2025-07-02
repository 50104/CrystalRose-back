package com.rose.back.domain.diary.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDataResponse {
    private List<RoseCareLogDto> careLogs;
    private List<DiaryResponse> diaries;
}
