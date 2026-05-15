package com.secureauction.auction.controller;

import com.secureauction.auction.domain.Notification;
import com.secureauction.auction.dto.ApiResponse;
import com.secureauction.auction.dto.NotificationDto;
import com.secureauction.auction.global.security.CustomUserDetails;
import com.secureauction.auction.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 목록 조회
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getNotifications(
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Page<Notification> page = notificationService.getNotifications(userDetails.getUser(), pageable);
        // userDetails에서 현재 로그인한 유저 객체를 꺼내 서비스로 전달
        List<NotificationDto.Response> responses = page.getContent().stream()
                .map(NotificationDto.Response::of)
                .collect(Collectors.toList());

        NotificationDto.PageInfo pageInfo = NotificationDto.PageInfo.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();

        Map<String, Object> result = new HashMap<>();
        result.put("content", responses);
        result.put("pageInfo", pageInfo);

        return ApiResponse.success(result, "알림 목록을 성공적으로 조회했습니다.");
    }

    /**
     * 단건 알림 읽음 처리
     */
    @PatchMapping("/{id}")
    public ApiResponse<Void> readNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.readNotification(id, userDetails.getUser());
        return ApiResponse.success(null, "알림이 읽음 처리되었습니다.");
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PatchMapping("/read")
    public ApiResponse<Void> readAllNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.readAllNotifications(userDetails.getUser());
        return ApiResponse.success(null, "모든 알림이 읽음 처리되었습니다.");
    }

    /**
     * 알림 단건 삭제
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.deleteNotification(id, userDetails.getUser());
        return ApiResponse.success(null, "알림이 삭제되었습니다.");
    }

    /**
     * 읽은 알림 모두 삭제
     */
    @DeleteMapping("/read")
    public ApiResponse<Void> deleteAllReadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.deleteAllReadNotifications(userDetails.getUser());
        return ApiResponse.success(null, "읽은 알림이 모두 삭제되었습니다.");
    }

}