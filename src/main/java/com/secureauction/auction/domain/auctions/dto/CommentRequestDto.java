package com.secureauction.auction.domain.auctions.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CommentRequestDto {
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Create {
        private String content;
    }
}
