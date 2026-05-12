package com.secureauction.auction.service;

import com.secureauction.auction.domain.*;
import com.secureauction.auction.repository.AuctionRepository;
import com.secureauction.auction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class BidServiceConcurrencyTest {

    @Autowired
    private BidService bidService;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    private Long auctionId;
    private List<User> bidders = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 1. 판매자 생성
        User seller = User.builder()
                .nickname("판매자")
                .loginId("seller")
                .password("password")
                .email("seller@test.com")
                .role(UserRole.ROLE_USER)
                .build();
        userRepository.save(seller);

        // 2. 경매 생성
        Auction auction = Auction.builder()
                .seller(seller)
                .title("동시성 테스트 물품")
                .description("설명")
                .category(Category.DIGITAL_DEVICES)
                .startPrice(10000L)
                .currentPrice(10000L)
                .status(AuctionStatus.LIVE)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(1))
                .build();
        auctionId = auctionRepository.save(auction).getId();

        // 3. 입찰자 100명 생성
        for (int i = 0; i < 100; i++) {
            User bidder = User.builder()
                    .nickname("입찰자" + i)
                    .loginId("bidder" + i)
                    .password("password")
                    .email("bidder" + i + "@test.com")
                    .role(UserRole.ROLE_USER)
                    .build();
            bidders.add(userRepository.save(bidder));
        }
    }

    @Test
    @DisplayName("100명이 동시에 입찰할 때, 낙관적 락 또는 비즈니스 로직에 의해 단 1명만 성공해야 한다.")
    void placeBidConcurrencyTest() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            User bidder = bidders.get(i);
            executorService.submit(() -> {
                try {
                    bidService.placeBid(auctionId, bidder, 15000L);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 낙관적 락 충돌(ObjectOptimisticLockingFailureException) 또는 
                    // 비즈니스 로직 위반(BusinessException: 현재가 이하 입찰) 모두 실패로 카운트
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Auction auction = auctionRepository.findById(auctionId).orElseThrow();
        
        System.out.println("Success count: " + successCount.get());
        System.out.println("Failure count: " + failureCount.get());
        System.out.println("Auction version: " + auction.getVersion());

        // 단 1명만 성공했는지 검증
        assertThat(successCount.get()).isEqualTo(1);
        // 나머지 99명은 어떠한 이유로든 실패했는지 검증
        assertThat(failureCount.get()).isEqualTo(99);
        // 버전이 1만큼 증가했는지 검증 (초기값 0 -> 성공 시 1)
        assertThat(auction.getVersion()).isEqualTo(1L);
    }
}
