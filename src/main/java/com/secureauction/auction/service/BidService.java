package com.secureauction.auction.service;

import com.secureauction.auction.domain.*;
import com.secureauction.auction.dto.BidResponse;
import com.secureauction.auction.exception.BusinessException;
import com.secureauction.auction.exception.ErrorCode;
import com.secureauction.auction.repository.AuctionRepository;
import com.secureauction.auction.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final NotificationService notificationService;

    @Transactional
    public BidResponse placeBid(Long auctionId, User bidder, Long bidPrice) {
        // 1. 경매 존재 여부 확인
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. 경매 상태 확인 (LIVE 인지)
        if (auction.getStatus() != AuctionStatus.LIVE) {
            throw new BusinessException(ErrorCode.AUCTION_CLOSED);
        }

        // 2-1. 경매 시간 확인 (마감 시간이 지났는지)
        if (auction.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.AUCTION_CLOSED);
        }

        // 3. 입찰 금액 확인 (price > current_price)
        if (bidPrice <= auction.getCurrentPrice()) {
            throw new BusinessException(ErrorCode.INVALID_BID_PRICE);
        }

        // 4. 본인 입찰 금지 (seller_id != login_user_id)
        if (auction.getSeller().getId().equals(bidder.getId())) {
            throw new BusinessException(ErrorCode.SELF_BID_NOT_ALLOWED);
        }

        // [알림 보내기 위함]
        Optional<Bid> lastHighBid = bidRepository.findFirstByAuctionOrderByPriceDesc(auction);

        // 5. 최고가 갱신 (Optimistic Lock @Version applied in Auction entity)
        auction.updateCurrentPrice(bidPrice);

        // 6. 입찰 기록 생성
        Bid bid = Bid.builder()
                .auction(auction)
                .user(bidder)
                .price(bidPrice)
                .build();
        
        bidRepository.save(bid);

        // 7. [알림 로직 추가]
        // 7-1. 판매자에게 알림 전송
        notificationService.createNotification(
                auction.getSeller(), // 수신자: 판매자
                NotificationType.OUTBID,
                String.format("[%s] 상품에 새로운 입찰이 등록되었습니다.", auction.getTitle()),
                "/product/" + auctionId
        );

        // 7-2. 밀려난 사람에게 알림 쏘기
        if (lastHighBid.isPresent()) {
            User previousBidder = lastHighBid.get().getUser();

            // 내가 내 기록을 갱신하는 게 아닐 때만 알림 전송 (중복 입찰 시 스팸 방지)
            if (!previousBidder.getId().equals(bidder.getId())) {
                notificationService.createNotification(
                        previousBidder, // 수신자: 밀려난 1등
                        NotificationType.OUTBID, // 타입: 상위 입찰 발생
                        String.format("[%s] 상품에 더 높은 입찰가가 제시되었습니다.", auction.getTitle()),
                        "/product/" + auctionId
                );
            }
        }

        return BidResponse.builder()
                .bidId(bid.getId())
                .currentPrice(auction.getCurrentPrice())
                .build();
    }
}
