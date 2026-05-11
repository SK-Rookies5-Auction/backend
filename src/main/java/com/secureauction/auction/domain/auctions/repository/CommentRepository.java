package com.secureauction.auction.domain.auctions.repository;

import com.secureauction.auction.domain.auctions.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAuctionId(Long auctionId);
}
