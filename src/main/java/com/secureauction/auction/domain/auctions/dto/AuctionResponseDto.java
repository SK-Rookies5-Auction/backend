package com.secureauction.auction.domain.auctions.dto;

import com.secureauction.auction.domain.auctions.entity.AuctionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class AuctionResponseDto {

    @Getter
    @Builder
    public static class ListResponse {
        private Long id;
        private String title;
        private Long currentPrice;
        private AuctionStatus status;
        private String category;
        private String mainPictureUrl;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer bidCount;
    }

    @Getter
    @Builder
    public static class DetailResponse {
        private Long id;
        private String title;
        private String description;
        private Long currentPrice;
        private Long startPrice;
        private AuctionStatus status;
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
    public static class PictureInfo {
        private String url;
        private boolean isMain;
    }

    @Getter
    @Builder
    public static class BidInfo {
        private String bidderNickname; // 입찰자
        private Long bidAmount;        // 입찰 금액
        private LocalDateTime bidTime; // 입찰 시간
    }
}
