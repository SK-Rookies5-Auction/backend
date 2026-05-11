package com.secureauction.auction.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값 검증 실패"),
    INVALID_BID_PRICE(HttpStatus.BAD_REQUEST, "현재 최고가보다 낮은 입찰 시도"),
    SELF_BID_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자신의 경매 물품에 입찰 시도"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한 부족"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "경매물품 혹은 사용자를 찾을 수 없음"),
    BID_CONFLICT(HttpStatus.CONFLICT, "동시 입찰 충돌"),
    ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 결제/배송/수령이 완료된 상태");

    private final HttpStatus status;
    private final String message;
}
