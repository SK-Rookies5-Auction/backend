package com.secureauction.auction.dto;

import com.secureauction.auction.domain.Notification;
import com.secureauction.auction.domain.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

public class NotificationDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private NotificationType type;
        private String content;
        private String targetUrl;
        private Boolean isRead;
        private LocalDateTime createdAt;

        // 엔티티를 DTO로 변환하는 정적 메서드 (서비스에서 사용)
        public static Response of(Notification notification) {
            return Response.builder()
                    .id(notification.getId())
                    .type(notification.getType())
                    .content(notification.getContent())
                    .targetUrl(notification.getTargetUrl())
                    .isRead(notification.getIsRead())
                    .createdAt(notification.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class PageInfo {
        private int currentPage;
        private int pageSize;
        private int totalPages;
        private long totalElements;
        private boolean isFirst;
        private boolean isLast;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}