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
        private Integer bidCount;
        private Boolean isLiked;
        private Long sellerId;
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
        private String mainPictureUrl;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer viewCount;
        private Integer likeCount;
        private Boolean isLiked;
        private Long sellerId;
        private String sellerNickname;
        private LocalDateTime sellerJoinedAt;
        private Long winnerId;
        private String winnerNickname;
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

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikeListResponse {
        private Long auctionId;
        private String title;
        private Long currentPrice;
        private String status;
        private Integer likeCount;
        private String mainPictureUrl;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LikeToggleResponse {
        private Long auctionId;
        private Integer likeCount;
        private Boolean isLiked;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyPageListResponse {
        private Long auctionId;
        private String title;
        private Long currentPrice;
        private Long myPrice;
        private Long finalPrice;
        private String status;
        private Integer viewCount;
        private Integer likeCount;
        private LocalDateTime createdAt;
        private String mainPictureUrl;
    }
}
