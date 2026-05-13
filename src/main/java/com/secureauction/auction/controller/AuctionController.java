package com.secureauction.auction.controller;

import com.secureauction.auction.dto.ApiResponse;
import com.secureauction.auction.dto.AuctionDto;
import com.secureauction.auction.dto.AuctionStatsResponse;
import com.secureauction.auction.global.security.CustomUserDetails;
import com.secureauction.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.HashMap;
import java.util.Map;

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
     * 경매 목록 조회 (필터링, 검색, 정렬, 페이지네이션 지원)
     */
    @GetMapping
    public ApiResponse<Object> getAuctionList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(defaultValue = "newest") String sort,
            @PageableDefault(size = 30) Pageable pageable) {
        
        Page<AuctionDto.ListResponse> resultPage = auctionService.getAuctionList(category, q, minPrice, maxPrice, sort, pageable);
        return createPaginatedResponse(resultPage, "경매 목록을 성공적으로 조회했습니다.");
    }

    /**
     * 경매 상세 조회
     */
    @GetMapping("/{id}")
    public ApiResponse<AuctionDto.DetailResponse> getAuctionDetail(@PathVariable Long id) {
        AuctionDto.DetailResponse response = auctionService.getAuctionDetail(id);
        return ApiResponse.success(response, "경매 상세 정보를 성공적으로 조회했습니다.");
    }

    @PostMapping("/{id}/likes")
    public ApiResponse<AuctionDto.LikeToggleResponse> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AuctionDto.LikeToggleResponse response = auctionService.toggleLike(id, userDetails.getUser());
        return ApiResponse.success(response, "관심 상품 상태가 업데이트되었습니다.");
    }

    /**
     * 메인 페이지 통계 정보 조회
     */
    @GetMapping("/stats")
    public ApiResponse<AuctionStatsResponse> getAuctionStats() {
        AuctionStatsResponse response = auctionService.getStats();
        return ApiResponse.success(response, "경매 통계 정보를 성공적으로 조회했습니다.");
    }

    // --- 페이지네이션 응답 공통 생성 로직 ---
    private ApiResponse<Object> createPaginatedResponse(Page<?> page, String message) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", page.getContent());
        
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("currentPage", page.getNumber());
        pageInfo.put("pageSize", page.getSize());
        pageInfo.put("totalPages", page.getTotalPages());
        pageInfo.put("totalElements", page.getTotalElements());
        pageInfo.put("isFirst", page.isFirst());
        pageInfo.put("isLast", page.isLast());
        pageInfo.put("hasNext", page.hasNext());
        pageInfo.put("hasPrevious", page.hasPrevious());
        
        responseData.put("pageInfo", pageInfo);
        
        return ApiResponse.success(responseData, message);
    }
}
