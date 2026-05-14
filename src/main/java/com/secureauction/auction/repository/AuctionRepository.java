package com.secureauction.auction.repository;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.secureauction.auction.domain.User;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuctionRepository extends JpaRepository<Auction, Long>, JpaSpecificationExecutor<Auction> {

    long countByStatus(AuctionStatus status);

    long countByStatusAndEndTimeBetween(AuctionStatus status, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT a FROM Auction a WHERE a.status = :status AND a.endTime <= :now")
    List<Auction> findAllByStatusAndEndTimeBefore(@Param("status") AuctionStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT a.id FROM Auction a WHERE a.status = :status AND a.endTime <= :now")
    List<Long> findIdsByStatusAndEndTimeBefore(@Param("status") AuctionStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT a FROM Auction a WHERE a.status = :status AND a.endTime BETWEEN :start AND :end")
    List<Auction> findAllByStatusAndEndTimeBetween(
            @Param("status") AuctionStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    Page<Auction> findBySeller(User seller, Pageable pageable);

    Page<Auction> findBySellerAndStatus(User seller, AuctionStatus status, Pageable pageable);

    Page<Auction> findBySellerAndStatusAndWinnerIsNull(User seller, AuctionStatus status, Pageable pageable);

    Page<Auction> findBySellerAndStatusAndWinnerIsNotNull(User seller, AuctionStatus status, Pageable pageable);

    long countBySeller(User seller);

    long countByWinner(User winner);

    Page<Auction> findByWinnerAndStatus(User winner, AuctionStatus status, Pageable pageable);
}
