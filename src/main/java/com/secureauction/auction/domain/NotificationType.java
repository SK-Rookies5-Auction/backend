package com.secureauction.auction.domain;

public enum NotificationType {
    OUTBID,          // 상위 입찰 발생
    AUCTION_WON,     // 경매 낙찰
    AUCTION_ENDED,   // 경매 종료 (유찰 등)
    NEW_QUESTION,    // 새로운 문의 등록
    NEW_ANSWER,      // 문의에 대한 답변 등록
    CLOSING_SOON     // 경매 마감 임박
}