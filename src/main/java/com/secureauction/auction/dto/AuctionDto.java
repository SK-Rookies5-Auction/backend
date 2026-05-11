package com.secureauction.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class AuctionDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String title;
        private String description;
        private String category;
        private Long startPrice;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<PictureInfo> pictures;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private Long id;
        private String title;
        private Long currentPrice;
        private String status;
        private String category;
        private String mainPictureUrl;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResponse {
        private Long id;
        private String title;
        private String description;
        private Long currentPrice;
        private Long startPrice;
        private String status;
        private String category;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer viewCount;
        private Integer likeCount;
        private String sellerNickname;
        private List<PictureInfo> pictures;
        private List<BidInfo> biddingHistory;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PictureInfo {
        private String url;
        private String imageKey;
        private Boolean isMain;
        private Integer sortOrder;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BidInfo {
        private String bidderNickname;
        private Long price;
        private LocalDateTime bidTime;
    }
}
