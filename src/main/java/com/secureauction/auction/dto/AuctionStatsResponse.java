package com.secureauction.auction.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuctionStatsResponse {

    private long totalActive;

    private long endingSoon;
}
