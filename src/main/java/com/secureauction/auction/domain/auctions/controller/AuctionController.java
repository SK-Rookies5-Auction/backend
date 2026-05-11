package com.secureauction.auction.domain.auctions.controller;

import com.secureauction.auction.domain.auctions.dto.AuctionRequestDto;
import com.secureauction.auction.domain.auctions.dto.AuctionResponseDto;
import com.secureauction.auction.domain.auctions.service.AuctionService;
import com.secureauction.auction.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    /**
     * [등록] 새로운 경매 상품 등록
     * POST /api/v1/auctions
     */
    @PostMapping
    public ApiResponse<Long> createAuction(@RequestBody AuctionRequestDto.CreateRequest request) {
        Long auctionId = auctionService.createAuction(request);
        return ApiResponse.success(auctionId, "경매 상품이 성공적으로 등록되었습니다.");
    }

    /**
     * [메인 페이지] 전체 경매 목록 조회
     * GET /api/v1/auctions
     */
    @GetMapping
    public ApiResponse<List<AuctionResponseDto.ListResponse>> getAuctions() {
        List<AuctionResponseDto.ListResponse> data = auctionService.getAuctionList();
        return ApiResponse.success(data, "경매 목록을 성공적으로 불러왔습니다.");
    }

    /**
     * [상세 페이지] 특정 경매 상세 조회
     * GET /api/v1/auctions/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<AuctionResponseDto.DetailResponse> getAuction(@PathVariable Long id) {
        AuctionResponseDto.DetailResponse data = auctionService.getAuctionDetail(id);
        return ApiResponse.success(data, "경매 상세 정보를 성공적으로 불러왔습니다.");
    }
}
