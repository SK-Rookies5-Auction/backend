package com.secureauction.auction.controller;

import com.secureauction.auction.dto.ApiResponse;
import com.secureauction.auction.dto.NotificationDto;
import com.secureauction.auction.global.security.CustomUserDetails;
import com.secureauction.auction.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 목록 조회
     */
    @GetMapping
    public ApiResponse<List<NotificationDto.Response>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // userDetails에서 현재 로그인한 유저 객체를 꺼내 서비스로 전달
        List<NotificationDto.Response> responses = notificationService.getNotifications(userDetails.getUser());
        return ApiResponse.success(responses, "알림 목록을 성공적으로 조회했습니다.");
    }

    /**
     * 알림 읽음 처리
     */
    @PatchMapping("/{id}")
    public ApiResponse<Void> readNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.readNotification(id, userDetails.getUser());
        return ApiResponse.success(null, "알림이 읽음 처리되었습니다.");
    }
}