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
import java.time.ZoneId;
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
        // DB 데이터가 KST(Asia/Seoul) 기준이므로, 서버 시간대와 상관없이 KST 기준으로 현재 시간을 가져옴
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("Closing expired auctions... (Standard KST: {})", now);

        // 엔티티 전체 대신 ID 목록만 조회하여 메모리 부하 감소 및 targetIds 변수명 일치
        List<Long> targetIds = auctionRepository.findIdsByStatusAndEndTimeBefore(AuctionStatus.LIVE, now);
        
        if (!targetIds.isEmpty()) {
            log.info("Found {} auctions to close.", targetIds.size());
        }

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
        // 마찬가지로 KST 기준으로 계산
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime targetTime = now.plusHours(1);
        
        List<Auction> closingAuctions = auctionRepository.findAllByStatusAndEndTimeBetween(
                AuctionStatus.LIVE, now, targetTime
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