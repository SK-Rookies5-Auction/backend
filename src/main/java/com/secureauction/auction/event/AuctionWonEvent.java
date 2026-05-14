package com.secureauction.auction.event;

import com.secureauction.auction.domain.Auction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 낙찰 이벤트: 낙찰된 Auction 엔티티를 통째로 전달
 */
@Getter
@RequiredArgsConstructor
public class AuctionWonEvent {
    private final Auction auction;
}