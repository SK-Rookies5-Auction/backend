package com.secureauction.auction.scheduler;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.AuctionStatus;
import com.secureauction.auction.domain.Bid;
import com.secureauction.auction.domain.Payment;
import com.secureauction.auction.domain.PaymentStatus;
import com.secureauction.auction.repository.AuctionRepository;
import com.secureauction.auction.repository.BidRepository;
import com.secureauction.auction.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionScheduler {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final PaymentRepository paymentRepository;

    @Scheduled(cron = "0 * * * * *") // 매 분 0초 실행
    @Transactional
    public void closeExpiredAuctions() {
        log.info("Closing expired auctions...");
        LocalDateTime now = LocalDateTime.now();
        
        List<Auction> expiredAuctions = auctionRepository.findAllByStatusAndEndTimeBefore(AuctionStatus.LIVE, now);
        
        for (Auction auction : expiredAuctions) {
            log.info("Auction {} expired. Processing closure...", auction.getId());
            
            // 1. 최고 입찰자 찾기
            bidRepository.findFirstByAuctionOrderByPriceDesc(auction)
                .ifPresentOrElse(
                    highestBid -> {
                        // 낙찰자 확정
                        auction.finish(highestBid.getUser());
                        
                        // 결제 정보 생성 (PENDING 상태)
                        Payment payment = Payment.builder()
                                .id(auction.getId()) // 간단히 경매 ID를 결제 ID로 사용 (요구사항에 맞춰 조정 가능)
                                .user(highestBid.getUser())
                                .auction(auction)
                                .finalPrice(highestBid.getPrice())
                                .status(PaymentStatus.PENDING)
                                .build();
                        paymentRepository.save(payment);
                        
                        sendNotification(highestBid.getUser().getId(), "경매 낙찰 알림", auction.getTitle() + " 경매에 낙찰되셨습니다.");
                        log.info("Auction {} won by user {}.", auction.getId(), highestBid.getUser().getId());
                    },
                    () -> {
                        // 유찰 처리
                        auction.updateStatus(AuctionStatus.FINISHED);
                        log.info("Auction {} finished with no bids.", auction.getId());
                    }
                );
        }
    }

    private void sendNotification(Long userId, String title, String message) {
        // TODO: 알림 서비스 연동 (Firebase, WebSocket 등)
        log.info("Notification to user {}: [{}] {}", userId, title, message);
    }
}
