package com.secureauction.auction.repository;

import com.secureauction.auction.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAuctionIdAndParentIsNull(Long auctionId);
}
