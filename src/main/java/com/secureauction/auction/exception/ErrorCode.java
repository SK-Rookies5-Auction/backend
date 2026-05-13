package com.secureauction.auction.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    IMAGE_FILE_REQUIRED(HttpStatus.BAD_REQUEST, "업로드할 이미지 파일이 없습니다."),
    INVALID_IMAGE_FILE_TYPE(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있습니다."),
    IMAGE_FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "이미지 파일 크기가 허용 범위를 초과했습니다."),
    S3_BUCKET_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 버킷 설정이 없습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다."),
    IMAGE_PRESIGNED_URL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 조회 URL 생성에 실패했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다."),
    INVALID_BID_PRICE(HttpStatus.BAD_REQUEST, "현재 최고가보다 높은 금액으로 입찰해야 합니다."),
    SELF_BID_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자신의 경매 물품에는 입찰할 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 부족합니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    BID_CONFLICT(HttpStatus.CONFLICT, "동시 입찰 충돌이 발생했습니다."),
    ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 상태입니다."),
    AUCTION_CLOSED(HttpStatus.BAD_REQUEST, "이미 종료된 경매입니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 경매를 찾을 수 없습니다."),
    NOT_THE_WINNER(HttpStatus.FORBIDDEN, "낙찰자만 처리할 수 있습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 낙찰가와 일치하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
