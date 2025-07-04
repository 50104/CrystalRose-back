package com.rose.back.domain.diary.controller.docs;

import com.rose.back.domain.diary.dto.CalendarDataResponse;
import com.rose.back.global.exception.CommonErrorResponses;
import com.rose.back.global.handler.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Calendar", description = "캘린더 통합 데이터 API")
public interface CalendarControllerDocs {

    @Operation(
        summary = "캘린더 통합 데이터 조회", 
        description = "지정된 날짜 범위의 케어로그와 다이어리 데이터를 함께 조회합니다. " +
                      "로그인한 사용자는 본인의 데이터를, 비로그인 사용자는 빈 데이터를 반환받습니다."
    )
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "캘린더 데이터 조회 성공",
            content = @Content(
                schema = @Schema(implementation = CalendarDataResponse.class),
                examples = {
                    @ExampleObject(
                        name = "로그인 사용자",
                        description = "로그인한 사용자의 캘린더 데이터",
                        value = """
                        {
                          "careLogs": [
                            {
                              "id": 1,
                              "fertilizer": "액체비료",
                              "pesticide": null,
                              "adjuvant": null,
                              "compost": null,
                              "fungicide": null,
                              "watering": "충분히",
                              "note": "새순이 많이 났어요",
                              "careDate": "2025-07-01"
                            }
                          ],
                          "diaries": [
                            {
                              "id": 1,
                              "note": "첫 번째 꽃이 피었습니다",
                              "imageUrl": "/image.jpg",
                              "recordedAt": "2025-07-01T10:30:00"
                            }
                          ]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "비로그인 사용자",
                        description = "비로그인 사용자의 빈 데이터",
                        value = """
                        {
                          "careLogs": [],
                          "diaries": []
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 날짜 형식",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "날짜 형식 오류",
                    value = """
                    {
                      "code": "INVALID_DATE_FORMAT",
                      "message": "날짜 형식이 올바르지 않습니다. YYYY-MM-DD 형식으로 입력해주세요.",
                      "path": "/api/calendar/data"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<CalendarDataResponse> getCalendarData(
        @Parameter(
            description = "조회 시작 날짜 (YYYY-MM-DD 형식)", 
            example = "2025-07-01",
            required = true
        )
        @RequestParam String startDate,
        
        @Parameter(
            description = "조회 종료 날짜 (YYYY-MM-DD 형식)", 
            example = "2025-07-31",
            required = true
        )
        @RequestParam String endDate
    );
}
