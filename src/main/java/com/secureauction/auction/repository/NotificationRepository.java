package com.secureauction.auction.repository;

import com.secureauction.auction.domain.Notification;
import com.secureauction.auction.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 유저별 알림 목록 (페이지네이션 적용)
    Page<Notification> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 읽지 않은 알림 목록 조회 (일괄 읽음 처리용)
    List<Notification> findAllByUserAndIsReadFalse(User user);

    // 읽은 알림 일괄 삭제
    void deleteAllByUserAndIsReadTrue(User user);
}