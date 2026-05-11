package com.secureauction.auction.service;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.AuctionStatus;
import com.secureauction.auction.domain.Bid;
import com.secureauction.auction.domain.User;
import com.secureauction.auction.exception.BusinessException;
import com.secureauction.auction.exception.ErrorCode;
import com.secureauction.auction.repository.AuctionRepository;
import com.secureauction.auction.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    @Transactional
    public Long placeBid(Long auctionId, User bidder, Long bidPrice) {
        // 1. 경매 존재 여부 확인
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. 경매 상태 확인 (LIVE 인지)
        if (auction.getStatus() != AuctionStatus.LIVE) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        // 3. 입찰 금액 확인 (price > current_price)
        if (bidPrice <= auction.getCurrentPrice()) {
            throw new BusinessException(ErrorCode.INVALID_BID_PRICE);
        }

        // 4. 본인 입찰 금지 (seller_id != login_user_id)
        if (auction.getSeller().getId().equals(bidder.getId())) {
            throw new BusinessException(ErrorCode.SELF_BID_NOT_ALLOWED);
        }

        // 5. 최고가 갱신 (Optimistic Lock @Version applied in Auction entity)
        auction.updateCurrentPrice(bidPrice);

        // 6. 입찰 기록 생성
        Bid bid = Bid.builder()
                .auction(auction)
                .user(bidder)
                .price(bidPrice)
                .build();
        
        bidRepository.save(bid);

        return bid.getId();
    }
}
