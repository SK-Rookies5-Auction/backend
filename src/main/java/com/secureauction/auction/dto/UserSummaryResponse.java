package com.secureauction.auction.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSummaryResponse {
    private Integer biddingCount;
    private Integer wonCount;
    private Integer hostedCount;
    private Integer watchlistCount;
}
