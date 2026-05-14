package com.secureauction.auction.event;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuctionWonEvent {
    private final Auction auction;
    private final User winner;
}
