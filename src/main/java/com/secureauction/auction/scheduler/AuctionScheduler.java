package com.secureauction.auction.scheduler;

import com.secureauction.auction.domain.*;
import com.secureauction.auction.repository.AuctionRepository;
import com.secureauction.auction.service.AuctionProcessService;
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
    private final NotificationService notificationService;
    private final AuctionProcessService auctionProcessService;

    @Scheduled(cron = "0 * * * * *") // 매 분 0초 실행
    public void closeExpiredAuctions() {
        log.info("Closing expired auctions...");
        LocalDateTime now = LocalDateTime.now();
        
        List<Auction> expiredAuctions = auctionRepository.findAllByStatusAndEndTimeBefore(AuctionStatus.LIVE, now);
        
        for (Auction auction : expiredAuctions) {
            try {
                // 비즈니스 로직은 서비스로 위임하고, 개별 경매의 실패가 전체 루프에 영향을 주지 않도록 예외 처리
                auctionProcessService.processClosure(auction.getId());
            } catch (Exception e) {
                log.error("Failed to process closure for auction {}: {}", auction.getId(), e.getMessage());
            }
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

}
