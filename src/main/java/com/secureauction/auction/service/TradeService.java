package com.secureauction.auction.service;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.AuctionStatus;
import com.secureauction.auction.domain.Payment;
import com.secureauction.auction.domain.PaymentStatus;
import com.secureauction.auction.domain.User;
import com.secureauction.auction.dto.PaymentRequest;
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
     * 3단계 보안 검증 (상태, 권한, 금액 무결성) 적용
     */
    @Transactional
    public void processPayment(PaymentRequest request, User user) {
        Auction auction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));

        // 1. 상태 검증: 해당 경매의 상태가 반드시 'FINISHED'여야 함
        if (auction.getStatus() != AuctionStatus.FINISHED) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        // 2. 권한 검증: 결제를 시도하는 현재 유저가 최종 낙찰자와 일치하는지 확인
        if (auction.getWinner() == null || !auction.getWinner().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_THE_WINNER);
        }

        // 3. 금액 무결성 검증: 클라이언트 전달 금액과 DB 낙찰가(currentPrice) 대조
        if (!auction.getCurrentPrice().equals(request.getAmount())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        Payment payment = paymentRepository.findByAuction(auction)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 결제 상태 검증 (중복 결제 방어)
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        // 4. 결제 처리 및 경매 상태 변경
        // 보안 검증이 완료된 DB의 currentPrice를 기준으로 최종 처리
        payment.complete();
        auction.updateStatus(AuctionStatus.PAID);
    }

    /**
     * 판매자 배송 시작 (PAID -> SHIPPING)
     */
    @Transactional
    public void startShipping(Long auctionId, User user) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));

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
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));

        // 1. 권한 검증: 낙찰자 본인만 수령 확인 가능
        if (auction.getWinner() == null || !auction.getWinner().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_THE_WINNER);
        }

        // 2. 상태 검증: SHIPPING 상태여야 수령 확인 가능
        if (auction.getStatus() != AuctionStatus.SHIPPING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        // 3. 최종 완료 상태로 변경
        auction.updateStatus(AuctionStatus.COMPLETED);
    }
}
