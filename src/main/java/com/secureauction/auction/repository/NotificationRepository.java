package com.secureauction.auction.repository;

import com.secureauction.auction.domain.Notification;
import com.secureauction.auction.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 특정 사용자의 알림을 최신순(CreatedAt Desc)으로 전체 조회
    List<Notification> findAllByUserOrderByCreatedAtDesc(User user);

}