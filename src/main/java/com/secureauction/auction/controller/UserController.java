package com.secureauction.auction.controller;

import com.secureauction.auction.dto.*;
import com.secureauction.auction.global.security.CustomUserDetails;
import com.secureauction.auction.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me/summary")
    public ApiResponse<UserSummaryResponse> getMySummary(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserSummaryResponse response = userService.getUserSummary(userDetails.getUser().getId());
        return ApiResponse.success(response, "마이페이지 요약 정보를 성공적으로 조회했습니다.");
    }

    @PatchMapping("/password")
    public ApiResponse<Object> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(userDetails.getUser().getId(), request);
        return ApiResponse.success(null, "비밀번호가 성공적으로 변경되었습니다.");
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoResponse response = userService.getUserInfo(userDetails.getUser().getId());
        return ApiResponse.success(response, "내 정보 조회 성공");
    }

    @PutMapping("/me")
    public ApiResponse<Object> updateInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUserInfo(userDetails.getUser().getId(), request);
        return ApiResponse.success(null, "회원 정보가 수정되었습니다.");
    }

    // 5. 마이페이지 등록 경매 목록 조회
    @GetMapping("/me/auctions")
    public ApiResponse<Object> getMyAuctions(
            @RequestParam(defaultValue = "ALL") String status,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Page<AuctionDto.MyPageListResponse> resultPage = userService.getMyAuctions(userDetails.getUser(), status, pageable);
        return createPaginatedResponse(resultPage, "마이페이지 등록 경매 내역을 성공적으로 조회했습니다.");
    }

    // 6. 마이페이지 입찰 내역 목록 조회
    @GetMapping("/me/bids")
    public ApiResponse<Object> getMyBids(
            @RequestParam(defaultValue = "ALL") String status,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Page<AuctionDto.MyPageListResponse> resultPage = userService.getMyBids(userDetails.getUser(), status, pageable);
        return createPaginatedResponse(resultPage, "마이페이지 입찰 내역을 성공적으로 조회했습니다.");
    }

    // 7. 마이페이지 관심 상품 목록 조회
    @GetMapping("/me/likes")
    public ApiResponse<Object> getMyLikes(
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Page<AuctionDto.MyPageListResponse> resultPage = userService.getMyWishlist(userDetails.getUser(), pageable);
        return createPaginatedResponse(resultPage, "마이페이지 관심 내역을 성공적으로 조회했습니다.");
    }

    // --- 페이지네이션 응답 공통 생성 로직 ---
    private ApiResponse<Object> createPaginatedResponse(Page<?> page, String message) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", page.getContent());
        responseData.put("pageInfo", Map.of(
                "currentPage", page.getNumber(),
                "pageSize", page.getSize(),
                "totalPages", page.getTotalPages(),
                "totalElements", page.getTotalElements(),
                "isFirst", page.isFirst(),
                "isLast", page.isLast(),
                "hasNext", page.hasNext(),
                "hasPrevious", page.hasPrevious()
        ));
        return ApiResponse.success(responseData, message);
    }
}
