package com.secureauction.auction.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSummaryResponse {
    @JsonProperty("bidding_count")
    private int biddingCount;
    @JsonProperty("won_count")
    private int wonCount;
    @JsonProperty("hosted_count")
    private int hostedCount;
    @JsonProperty("watchlist_count")
    private int watchlistCount;
}