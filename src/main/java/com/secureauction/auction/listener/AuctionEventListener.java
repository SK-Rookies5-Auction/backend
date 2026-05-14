package com.secureauction.auction.listener;

import com.secureauction.auction.domain.NotificationType;
import com.secureauction.auction.event.AuctionWonEvent;
import com.secureauction.auction.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionWon(AuctionWonEvent event) {
        log.info("Handling AuctionWonEvent for auction {} asynchronously after commit", event.getAuction().getId());
        notificationService.createNotification(
                event.getWinner(),
                NotificationType.AUCTION_WON,
                String.format("[낙찰] '%s' 경매에 최종 낙찰되셨습니다!", event.getAuction().getTitle()),
                "/product/" + event.getAuction().getId()
        );
    }
}
