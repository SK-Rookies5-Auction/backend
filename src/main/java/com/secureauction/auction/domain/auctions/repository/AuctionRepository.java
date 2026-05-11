package com.secureauction.auction.domain.auctions.repository;

import com.secureauction.auction.domain.auctions.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
}
