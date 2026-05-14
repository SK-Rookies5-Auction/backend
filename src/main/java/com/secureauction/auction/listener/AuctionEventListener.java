package com.secureauction.auction.listener;

import com.secureauction.auction.domain.NotificationType;
import com.secureauction.auction.event.AuctionWonEvent;
import com.secureauction.auction.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handleAuctionWon(AuctionWonEvent event) {
        log.info("Handling AuctionWonEvent for auction {}", event.getAuction().getId());
        notificationService.createNotification(
                event.getWinner(),
                NotificationType.AUCTION_WON,
                String.format("[낙찰] '%s' 경매에 최종 낙찰되셨습니다!", event.getAuction().getTitle()),
                "/auctions/" + event.getAuction().getId()
        );
    }
}
