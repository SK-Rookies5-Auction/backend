package com.secureauction.auction.controller;

import com.secureauction.auction.domain.User;
import com.secureauction.auction.dto.ApiResponse;
import com.secureauction.auction.dto.PaymentRequest;
import com.secureauction.auction.exception.BusinessException;
import com.secureauction.auction.exception.ErrorCode;
import com.secureauction.auction.repository.UserRepository;
import com.secureauction.auction.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final UserRepository userRepository;

    /**
     * 낙찰자 결제
     */
    @PostMapping("/payments")
    public ApiResponse<Void> processPayment(@RequestBody PaymentRequest request) {
        // Mocking winner retrieval (ID 2L as winner)
        User user = userRepository.findById(2L) 
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        tradeService.processPayment(request.getAuctionId(), user);
        return ApiResponse.success(null);
    }

    /**
     * 판매자 배송 시작
     */
    @PatchMapping("/auctions/{id}/shipping")
    public ApiResponse<Void> startShipping(@PathVariable Long id) {
        // Mocking seller retrieval (ID 1L as seller)
        User user = userRepository.findById(1L) 
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        tradeService.startShipping(id, user);
        return ApiResponse.success(null);
    }

    /**
     * 낙찰자 수령 확인
     */
    @PatchMapping("/auctions/{id}/complete")
    public ApiResponse<Void> completeTrade(@PathVariable Long id) {
        // Mocking winner retrieval (ID 2L as winner)
        User user = userRepository.findById(2L) 
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        tradeService.completeTrade(id, user);
        return ApiResponse.success(null);
    }
}
