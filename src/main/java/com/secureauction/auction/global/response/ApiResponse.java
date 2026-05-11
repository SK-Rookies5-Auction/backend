package com.secureauction.auction.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;   // 요청 성공 여부
    private int status;
    private T data;            // 실제 전달할 데이터 (객체, 리스트 등)
    private String message;    // 응답 메시지 (예: "조회 성공")
    private LocalDateTime timestamp; // 응답 시간

    /**
     * 성공 응답을 생성하는 정적 팩토리 메서드
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, 200, data, message, LocalDateTime.now());
    }

    /**
     * 실패 응답을 생성하는 정적 팩토리 메서드
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(false, status, null, message, LocalDateTime.now());
    }
}