package com.secureauction.auction.controller;

import com.secureauction.auction.domain.User;
import com.secureauction.auction.dto.ApiResponse;
import com.secureauction.auction.dto.BidRequest;
import com.secureauction.auction.dto.BidResponse;
import com.secureauction.auction.global.security.CustomUserDetails;
import com.secureauction.auction.repository.UserRepository;
import com.secureauction.auction.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
public class AuctionBidController {

    private final BidService bidService;
    private final UserRepository userRepository;

    @PostMapping("/{id}/bids")
    public ApiResponse<BidResponse> placeBid(
            @PathVariable Long id,
            @Valid @RequestBody BidRequest bidRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User bidder = userDetails.getUser();

        BidResponse response = bidService.placeBid(id, bidder, bidRequest.getPrice());
        return ApiResponse.success(response, "입찰이 성공적으로 완료되었습니다.");
    }
}
