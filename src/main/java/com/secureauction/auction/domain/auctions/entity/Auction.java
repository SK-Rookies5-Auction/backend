package com.secureauction.auction.domain.auctions.entity;

import com.secureauction.auction.domain.user.entity.User;
import com.secureauction.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auctions")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuctionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuctionCategory category;

    @Column(name = "start_price", nullable = false)
    private Long startPrice;

    @Column(name = "current_price", nullable = false)
    private Long currentPrice;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Picture> pictures = new ArrayList<>();

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

}
