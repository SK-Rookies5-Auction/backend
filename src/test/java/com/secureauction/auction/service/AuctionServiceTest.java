package com.secureauction.auction.service;

import com.secureauction.auction.domain.AuctionStatus;
import com.secureauction.auction.dto.AuctionStatsResponse;
import com.secureauction.auction.repository.AuctionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @InjectMocks
    private AuctionService auctionService;

    @Test
    @DisplayName("경매 통계 정보를 올바르게 반환한다")
    void getStats_ShouldReturnCorrectStats() {
        // given
        long totalActive = 10L;
        long endingSoon = 3L;

        when(auctionRepository.countByStatus(AuctionStatus.LIVE)).thenReturn(totalActive);
        when(auctionRepository.countByStatusAndEndTimeBetween(eq(AuctionStatus.LIVE), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(endingSoon);

        // when
        AuctionStatsResponse stats = auctionService.getStats();

        // then
        assertThat(stats.getTotalActive()).isEqualTo(totalActive);
        assertThat(stats.getEndingSoon()).isEqualTo(endingSoon);
    }
}
