package com.secureauction.auction.domain.auctions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class AuctionRequestDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String title;
        private String description;
        private String category;
        private Long startPrice;
        private LocalDateTime endTime;
        private List<PictureRequest> pictures;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PictureRequest {
        private String url;
        private String imageKey;
        private Boolean isMain;
        private Integer sortOrder;
    }
}
