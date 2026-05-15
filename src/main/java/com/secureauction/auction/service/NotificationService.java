package com.secureauction.auction.service;

import com.secureauction.auction.domain.Notification;
import com.secureauction.auction.domain.NotificationType;
import com.secureauction.auction.domain.User;
import com.secureauction.auction.dto.NotificationDto;
import com.secureauction.auction.exception.BusinessException;
import com.secureauction.auction.exception.ErrorCode;
import com.secureauction.auction.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // 알림 목록 조회 (페이지네이션)
    public Page<Notification> getNotifications(User user, Pageable pageable) {
        return notificationRepository.findAllByUserOrderByCreatedAtDesc(user, pageable);
    }

    // 알림 읽음 처리(단건)
    @Transactional
    public void readNotification(Long id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        
        // 보안 체크: 내 알림인지 확인
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        notification.markAsRead();
    }

    // 알림 단건 삭제
    @Transactional
    public void deleteNotification(Long id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 보안 체크: 내 알림인지 확인
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        notificationRepository.delete(notification);
    }

    // 모든 알림 읽음 처리 (일괄)
    @Transactional
    public void readAllNotifications(User user) {
        List<Notification> unreadNotifications = notificationRepository.findAllByUserAndIsReadFalse(user);
        unreadNotifications.forEach(Notification::markAsRead);
    }

    // 읽은 알림 모두 삭제 (일괄)
    @Transactional
    public void deleteAllReadNotifications(User user) {
        notificationRepository.deleteAllByUserAndIsReadTrue(user);
    }

    @Transactional
    public void createNotification(User user, NotificationType type, String content, String targetUrl) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .content(content)
                .targetUrl(targetUrl)
                .isRead(false)
                .build();
        notificationRepository.save(notification);

    }
}