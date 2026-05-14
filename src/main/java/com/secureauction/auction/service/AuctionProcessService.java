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

    // Propagation.REQUIRES_NEW를 통해 스케줄러와 트랜잭션을 완전히 분리 (장애 방어 극대화)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processClosure(Long auctionId) {
        log.info("Processing closure for Auction {}...", auctionId);
        
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found ID: " + auctionId));

        // 이미 마감 처리된 경우 중복 처리 방지
        if (auction.getStatus() != AuctionStatus.LIVE) {
            log.warn("Auction {} is not in LIVE status. Skipping closure.", auctionId);
            return;
        }

        // 1. 최고 입찰자 찾기 및 낙찰/유찰 처리
        bidRepository.findFirstByAuctionOrderByPriceDesc(auction)
            .ifPresentOrElse(
                highestBid -> {
                    // [낙찰 시]: 낙찰자 확정 및 결제 정보 생성
                    auction.finish(highestBid.getUser());

                    Payment payment = Payment.builder()
                            .user(highestBid.getUser())
                            .auction(auction)
                            .finalPrice(highestBid.getPrice())
                            .status(PaymentStatus.PENDING)
                            .build();
                    paymentRepository.save(payment);

                    // 이벤트 발행 방식 적용 (유지보수성 향상)
                    eventPublisher.publishEvent(new AuctionWonEvent(auction, highestBid.getUser()));

                    // [추가] 판매자에게 낙찰 결과 알림 전송
                    notificationService.createNotification(
                            auction.getSeller(),
                            NotificationType.AUCTION_WON,
                            String.format("[판매] '%s' 상품이 최종가 ₩%,d에 낙찰되었습니다!", auction.getTitle(), highestBid.getPrice()),
                            "/product/" + auction.getId()
                    );

                    log.info("Auction {} won by user {}.", auction.getId(), highestBid.getUser().getId());
                },
                () -> {
                    // [유찰 시]: 상태 변경 및 판매자 알림 전송
                    auction.updateStatus(AuctionStatus.FINISHED);

                    notificationService.createNotification(
                            auction.getSeller(),
                            NotificationType.AUCTION_ENDED,
                            String.format("[유찰] '%s' 경매가 입찰자 없이 종료되었습니다.", auction.getTitle()),
                            "/product/" + auction.getId()
                    );
                    log.info("Auction {} finished with no bids.", auction.getId());
                }
            );
    }
}