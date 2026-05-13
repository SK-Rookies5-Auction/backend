package com.secureauction.auction.repository;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.secureauction.auction.domain.User;

import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    Optional<Bid> findFirstByAuctionOrderByPriceDesc(Auction auction);
    @Query("SELECT DISTINCT b.auction FROM Bid b WHERE b.user = :user")
    Page<Auction> findBidAuctionsByUser(@Param("user") User user, Pageable pageable);
}
