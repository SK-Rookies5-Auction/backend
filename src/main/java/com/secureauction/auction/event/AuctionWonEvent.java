package com.secureauction.auction.event;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 낙찰 이벤트: 낙찰된 Auction 엔티티와 낙찰자 정보를 전달
 */
@Getter
@RequiredArgsConstructor
public class AuctionWonEvent {
    private final Auction auction;
    private final User winner;
}
