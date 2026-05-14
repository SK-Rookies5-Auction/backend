package com.secureauction.auction.repository;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.AuctionStatus;
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

    @Query("SELECT COUNT(DISTINCT b.auction.id) FROM Bid b WHERE b.user = :user")
    long countDistinctAuctionsByUser(@Param("user") User user);

    @Query("SELECT MAX(b.price) FROM Bid b WHERE b.auction = :auction AND b.user = :user")
    Long findHighestPriceByAuctionAndUser(@Param("auction") Auction auction, @Param("user") User user);

    @Query("SELECT MAX(b.price) FROM Bid b WHERE b.auction = :auction")
    Long findHighestPriceByAuction(@Param("auction") Auction auction);

    @Query("SELECT DISTINCT b.auction FROM Bid b WHERE b.user = :user AND b.auction.status = :status")
    Page<Auction> findBidAuctionsByUserAndStatus(@Param("user") User user, @Param("status") AuctionStatus status, Pageable pageable);

    @Query("SELECT DISTINCT b.auction FROM Bid b WHERE b.user = :user AND b.auction.status = 'LIVE' AND b.auction.currentPrice > (SELECT MAX(b2.price) FROM Bid b2 WHERE b2.auction = b.auction AND b2.user = :user)")
    Page<Auction> findOutbidAuctionsByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT DISTINCT b.auction FROM Bid b WHERE b.user = :user AND b.auction.status = 'FINISHED' AND (b.auction.winner IS NULL OR b.auction.winner != :user)")
    Page<Auction> findLostAuctionsByUser(@Param("user") User user, Pageable pageable);
}
