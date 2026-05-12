package com.secureauction.auction.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 알림을 받는 사람

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type; // OUTBID, CLOSING_SOON, WON 등

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 알림 내용

    @Column(name = "target_url")
    private String targetUrl; // 클릭 시 이동할 페이지 주소

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false; // 읽음 여부

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 알림 읽음 처리 메서드
    public void markAsRead() {
        this.isRead = true;
    }
}