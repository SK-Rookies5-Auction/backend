package com.secureauction.auction.controller;

import com.secureauction.auction.domain.User;
import com.secureauction.auction.dto.ApiResponse;
import com.secureauction.auction.dto.PaymentRequest;
import com.secureauction.auction.exception.BusinessException;
import com.secureauction.auction.exception.ErrorCode;
import com.secureauction.auction.global.security.CustomUserDetails;
import com.secureauction.auction.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    /**
     * 낙찰자 결제
     */
    @PostMapping("/payments")
    public ApiResponse<Void> processPayment(
            @RequestBody PaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        tradeService.processPayment(request, userDetails.getUser());
        return ApiResponse.success(null, "결제가 성공적으로 완료되었습니다.");
    }

    /**
     * 판매자 배송 시작
     */
    @PatchMapping("/auctions/{id}/shipping")
    public ApiResponse<Void> startShipping(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        tradeService.startShipping(id, userDetails.getUser());
        return ApiResponse.success(null, "배송이 시작되었습니다.");
    }

    /**
     * 낙찰자 수령 확인
     */
    @PatchMapping("/auctions/{id}/complete")
    public ApiResponse<Void> completeTrade(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        tradeService.completeTrade(id, userDetails.getUser());
        return ApiResponse.success(null, "수령 확인이 완료되었습니다.");
    }
}
