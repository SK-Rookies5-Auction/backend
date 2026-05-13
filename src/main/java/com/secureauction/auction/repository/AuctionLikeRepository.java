package com.secureauction.auction.repository;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.AuctionLike;
import com.secureauction.auction.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuctionLikeRepository extends JpaRepository<AuctionLike, Long> {

    Optional<AuctionLike> findByUserAndAuction(User user, Auction auction);

    List<AuctionLike> findByUser(User user);

    @EntityGraph(attributePaths = {"auction"})
    Page<AuctionLike> findByUser(User user, Pageable pageable);

    long countByUser(User user);
}
