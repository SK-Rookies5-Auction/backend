package com.secureauction.auction.event;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.NotificationType;
import com.secureauction.auction.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuctionEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionWon(AuctionWonEvent event) {
        Auction auction = event.getAuction();
        
        // 엔티티에 저장된 winner를 활용
        if (auction.getWinner() != null) {
            notificationService.createNotification(
                    auction.getWinner(),
                    NotificationType.AUCTION_WON,
                    String.format("[낙찰] '%s' 경매에 최종 낙찰되셨습니다!", auction.getTitle()),
                    "/product/" + auction.getId()
            );
        }
    }
}