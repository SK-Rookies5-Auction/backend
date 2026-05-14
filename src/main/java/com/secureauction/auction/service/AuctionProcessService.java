package com.secureauction.auction.service;

import com.secureauction.auction.domain.*;
import com.secureauction.auction.event.AuctionWonEvent;
import com.secureauction.auction.repository.AuctionRepository;
import com.secureauction.auction.repository.BidRepository;
import com.secureauction.auction.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionProcessService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processClosure(Long auctionId) {
        log.info("Processing closure for Auction {}...", auctionId);
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));

        if (auction.getStatus() != AuctionStatus.LIVE) {
            log.warn("Auction {} is not in LIVE status. Skipping closure.", auctionId);
            return;
        }

        // 1. 최고 입찰자 찾기
        bidRepository.findFirstByAuctionOrderByPriceDesc(auction)
            .ifPresentOrElse(
                highestBid -> {
                    // 낙찰자 확정
                    auction.finish(highestBid.getUser());
                    
                    // 결제 정보 생성 (PENDING 상태)
                    Payment payment = Payment.builder()
                            .user(highestBid.getUser())
                            .auction(auction)
                            .finalPrice(highestBid.getPrice())
                            .status(PaymentStatus.PENDING)
                            .build();
                    paymentRepository.save(payment);

                    // 낙찰 알림 발행 (ApplicationEventPublisher 사용)
                    eventPublisher.publishEvent(new AuctionWonEvent(auction, highestBid.getUser()));
                    log.info("Auction {} won by user {}.", auction.getId(), highestBid.getUser().getId());
                },
                () -> {
                    // 유찰 처리
                    auction.updateStatus(AuctionStatus.FINISHED);

                    // 판매자에게 유찰 알림 (기존 로직 보존)
                    notificationService.createNotification(
                            auction.getSeller(),
                            NotificationType.AUCTION_ENDED, // 유찰 알림
                            String.format("[유찰] '%s' 경매가 입찰자 없이 종료되었습니다.", auction.getTitle()),
                            "/auctions/" + auction.getId()
                    );
                    log.info("Auction {} finished with no bids.", auction.getId());
                }
            );
    }
}
