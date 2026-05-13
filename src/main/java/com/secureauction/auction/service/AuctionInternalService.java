package com.secureauction.auction.service;

import com.secureauction.auction.domain.*;
import com.secureauction.auction.repository.AuctionRepository;
import com.secureauction.auction.repository.BidRepository;
import com.secureauction.auction.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionInternalService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    /**
     * 특정 경매의 종료 처리를 수행함.
     * Propagation.REQUIRES_NEW를 사용하여 독립적인 트랜잭션에서 실행됨.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void closeAuctionIfExpired(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null || auction.getStatus() != AuctionStatus.LIVE || auction.getEndTime().isAfter(LocalDateTime.now())) {
            return;
        }

        log.info("Closing auction {}. Processing closure...", auctionId);

        // 최고 입찰자 찾기
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

                    // 낙찰 알림 전송
                    notificationService.createNotification(
                            highestBid.getUser(),
                            NotificationType.AUCTION_WON,
                            String.format("[낙찰] '%s' 경매에 최종 낙찰되셨습니다!", auction.getTitle()),
                            "/product/" + auction.getId()
                    );
                    log.info("Auction {} won by user {}.", auctionId, highestBid.getUser().getId());
                },
                () -> {
                    // 유찰 처리
                    auction.updateStatus(AuctionStatus.FINISHED);

                    // 판매자에게 유찰 알림
                    notificationService.createNotification(
                            auction.getSeller(),
                            NotificationType.AUCTION_ENDED,
                            String.format("[유찰] '%s' 경매가 입찰자 없이 종료되었습니다.", auction.getTitle()),
                            "/product/" + auction.getId()
                    );
                    log.info("Auction {} finished with no bids.", auctionId);
                }
            );
    }
}
