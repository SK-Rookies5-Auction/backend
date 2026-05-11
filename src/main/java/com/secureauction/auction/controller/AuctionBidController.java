package com.secureauction.auction.controller;

import com.secureauction.auction.domain.User;
import com.secureauction.auction.dto.ApiResponse;
import com.secureauction.auction.dto.BidRequest;
import com.secureauction.auction.exception.BusinessException;
import com.secureauction.auction.exception.ErrorCode;
import com.secureauction.auction.repository.UserRepository;
import com.secureauction.auction.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
public class AuctionBidController {

    private final BidService bidService;
    private final UserRepository userRepository;

    @PostMapping("/{id}/bids")
    public ApiResponse<Long> placeBid(
            @PathVariable Long id,
            @Valid @RequestBody BidRequest bidRequest
            // In a real scenario, use @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // Mocking user retrieval for demonstration. 
        // In production, this would come from the Security Context.
        User bidder = userRepository.findById(1L) 
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        Long bidId = bidService.placeBid(id, bidder, bidRequest.getPrice());
        return ApiResponse.success(bidId);
    }
}
