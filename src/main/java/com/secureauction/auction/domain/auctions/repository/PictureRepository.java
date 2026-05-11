package com.secureauction.auction.domain.auctions.repository;

import com.secureauction.auction.domain.auctions.entity.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {
    List<Picture> findAllByAuctionId(Long auctionId);
}
