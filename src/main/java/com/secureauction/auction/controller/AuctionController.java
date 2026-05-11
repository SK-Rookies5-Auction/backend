package com.secureauction.auction.controller;

import com.secureauction.auction.dto.ApiResponse;
import com.secureauction.auction.dto.AuctionDto;
import com.secureauction.auction.global.security.CustomUserDetails;
import com.secureauction.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    /**
     * 경매 물품 등록
     */
    @PostMapping
    public ApiResponse<Long> createAuction(
            @RequestBody AuctionDto.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long auctionId = auctionService.createAuction(request, userDetails.getUser());
        return ApiResponse.success(auctionId, "경매 물품이 성공적으로 등록되었습니다.");
    }

    /**
     * 경매 목록 조회
     */
    @GetMapping
    public ApiResponse<List<AuctionDto.ListResponse>> getAuctionList() {
        List<AuctionDto.ListResponse> response = auctionService.getAuctionList();
        return ApiResponse.success(response, "경매 목록을 성공적으로 조회했습니다.");
    }

    /**
     * 경매 상세 조회
     */
    @GetMapping("/{id}")
    public ApiResponse<AuctionDto.DetailResponse> getAuctionDetail(@PathVariable Long id) {
        AuctionDto.DetailResponse response = auctionService.getAuctionDetail(id);
        return ApiResponse.success(response, "경매 상세 정보를 성공적으로 조회했습니다.");
    }
}
