package com.secureauction.auction.global.exception;

import com.secureauction.auction.global.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 우리가 직접 던진 예외 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    // 2. DB 제약 조건 위반 (아까 seller_id null 같은 상황)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return ApiResponse.error(400, "데이터 입력 값이 올바르지 않습니다.");
    }

    // 3. 서버 내부에서 예상치 못한 에러가 났을 때 (보안상 중요!)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        // 실제 로그에는 e.printStackTrace()를 찍더라도, 사용자에게는 비밀!
        return ApiResponse.error(500, "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.");
    }
}