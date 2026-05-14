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

        // 엔티티 전체 대신 ID 목록만 조회하여 메모리 부하 감소 및 targetIds 변수명 일치
        List<Long> targetIds = auctionRepository.findIdsByStatusAndEndTimeBefore(AuctionStatus.LIVE, now);

        for (Long auctionId : targetIds) {
            try {
                // 개별 경매 처리를 독립 트랜잭션으로 위임
                auctionProcessService.processClosure(auctionId);
            } catch (Exception e) {
                // 특정 경매 처리 중 에러(DB 락, 네트워크 오류 등)가 나도 전체 스케줄러가 죽지 않고 넘어감
                log.error("[Critical] 경매 마감 처리 실패 - ID: {}, 사유: {}", auctionId, e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void notifyClosingSoon() {
        LocalDateTime targetTime = LocalDateTime.now().plusHours(1);
        List<Auction> closingAuctions = auctionRepository.findAllByStatusAndEndTimeBetween(
                AuctionStatus.LIVE, LocalDateTime.now(), targetTime
        );

        for (Auction auction : closingAuctions) {
            auction.getBids().stream()
                    .map(Bid::getUser)
                    .distinct()
                    .forEach(user -> notificationService.createNotification(
                            user,
                            NotificationType.CLOSING_SOON,
                            String.format("[마감 임박] '%s' 경매 마감이 1시간 남았습니다!", auction.getTitle()),
                            "/product/" + auction.getId()
                    ));
        }
    }
}