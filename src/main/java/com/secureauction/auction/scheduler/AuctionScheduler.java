package com.secureauction.auction.scheduler;

import com.secureauction.auction.domain.*;
import com.secureauction.auction.repository.AuctionRepository;
import com.secureauction.auction.repository.BidRepository;
import com.secureauction.auction.repository.PaymentRepository;
import com.secureauction.auction.repository.UserRepository;
import com.secureauction.auction.service.NotificationService;
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
    private final NotificationService notificationService;
    private final UserRepository userRepository;

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
                                .user(highestBid.getUser())
                                .auction(auction)
                                .finalPrice(highestBid.getPrice())
                                .status(PaymentStatus.PENDING)
                                .build();
                        paymentRepository.save(payment);
                        
                        sendNotification(highestBid.getUser().getId(), auction.getId(),
                                "경매 낙찰 알림", auction.getTitle() + " 경매에 낙찰되셨습니다.");
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

    @Scheduled(cron = "0 0 * * * *") // 매 시간 정각에 실행 (예: 12시, 1시...)
    @Transactional
    public void notifyClosingSoon() {
        LocalDateTime targetTime = LocalDateTime.now().plusHours(1); // 지금부터 딱 1시간 뒤
        // 마감 1시간 전인 경매들 조회
        List<Auction> closingAuctions = auctionRepository.findAllByStatusAndEndTimeBetween(
                AuctionStatus.LIVE, LocalDateTime.now(), targetTime
        );

        for (Auction auction : closingAuctions) {
            // 입찰자들에게 알림 전송
            auction.getBids().stream()
                    .map(Bid::getUser)
                    .distinct()
                    .forEach(user -> notificationService.createNotification(
                            user,
                            NotificationType.CLOSING_SOON,
                            String.format("[마감 임박] '%s' 경매 마감이 1시간 남았습니다!", auction.getTitle()),
                            "/auctions/" + auction.getId()
                    ));
        }
    }

    private void sendNotification(Long userId, Long auctionId, String title, String message) {
        // 1. ID로 유저 객체를 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 알림 서비스 호출
        notificationService.createNotification(
                user,
                NotificationType.WON,
                String.format("[%s] %s", title, message), // 전달받은 제목과 메시지 활용
                "/auctions" + auctionId
        );
    }
}
