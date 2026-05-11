package com.secureauction.auction.domain.auctions.entity;

import com.secureauction.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pictures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Picture extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Column(name = "url", columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @Column(name = "image_key", columnDefinition = "TEXT", nullable = false)
    private String imageKey;

    @Column(name = "is_main", nullable = false)
    private Boolean isMain;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Builder
    public Picture(Auction auction, String imageUrl, String imageKey, Boolean isMain, Integer sortOrder) {
        this.auction = auction;
        this.imageUrl = imageUrl;
        this.imageKey = imageKey;
        this.isMain = isMain;
        this.sortOrder = sortOrder;
    }
}
