package com.secureauction.auction.domain;

public enum NotificationType {
    OUTBID,       // 상위 입찰 발생
    CLOSING_SOON, // 마감 임박
    WON,          // 낙찰 성공
    SOLD,         // 판매 완료
    COMMENT       // 내 물품에 댓글 등록
}