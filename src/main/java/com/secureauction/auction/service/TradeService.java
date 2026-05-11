package com.secureauction.auction.service;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.AuctionStatus;
import com.secureauction.auction.domain.Payment;
import com.secureauction.auction.domain.PaymentStatus;
import com.secureauction.auction.domain.User;
import com.secureauction.auction.exception.BusinessException;
import com.secureauction.auction.exception.ErrorCode;
import com.secureauction.auction.repository.AuctionRepository;
import com.secureauction.auction.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private final AuctionRepository auctionRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 낙찰자 결제 (PENDING -> PAID)
     */
    @Transactional
    public void processPayment(Long auctionId, User user) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 1. 권한 검증: 낙찰자 본인만 결제 가능
        if (auction.getWinner() == null || !auction.getWinner().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 상태 검증: 경매가 FINISHED 상태여야 함
        if (auction.getStatus() != AuctionStatus.FINISHED) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        Payment payment = paymentRepository.findByAuction(auction)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 3. 결제 상태 검증: PENDING 상태여야 함
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        // 4. 결제 처리 및 경매 상태 변경
        payment.complete();
        auction.updateStatus(AuctionStatus.PAID);
    }

    /**
     * 판매자 배송 시작 (PAID -> SHIPPING)
     */
    @Transactional
    public void startShipping(Long auctionId, User user) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 1. 권한 검증: 판매자 본인만 배송 처리 가능
        if (!auction.getSeller().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 상태 검증: PAID 상태여야 배송 가능
        if (auction.getStatus() != AuctionStatus.PAID) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        // 3. 배송 상태로 변경
        auction.updateStatus(AuctionStatus.SHIPPING);
    }

    /**
     * 낙찰자 수령 확인 (SHIPPING -> COMPLETED)
     */
    @Transactional
    public void completeTrade(Long auctionId, User user) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 1. 권한 검증: 낙찰자 본인만 수령 확인 가능
        if (auction.getWinner() == null || !auction.getWinner().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 상태 검증: SHIPPING 상태여야 수령 확인 가능
        if (auction.getStatus() != AuctionStatus.SHIPPING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        // 3. 최종 완료 상태로 변경
        auction.updateStatus(AuctionStatus.COMPLETED);
    }
}
