package com.secureauction.auction.service;

import com.secureauction.auction.domain.*;
import com.secureauction.auction.dto.AuctionDto;
import com.secureauction.auction.dto.AuctionStatsResponse;
import com.secureauction.auction.repository.AuctionLikeRepository;
import com.secureauction.auction.repository.AuctionRepository;
import com.secureauction.auction.repository.PictureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final PictureRepository pictureRepository;
    private final ImageService imageService;
    private final AuctionLikeRepository auctionLikeRepository;

    /**
     * [등록] 경매 상품 등록
     */
    @Transactional
    public Long createAuction(AuctionDto.CreateRequest request, User seller) {
        // 1. Auction 엔티티 생성
        Auction auction = Auction.builder()
                .seller(seller)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(Category.valueOf(request.getCategory()))
                .startPrice(request.getStartPrice())
                .currentPrice(request.getStartPrice())
                .startTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now())
                .endTime(request.getEndTime())
                .status(AuctionStatus.LIVE)
                .build();

        // 2. 경매 저장
        Auction savedAuction = auctionRepository.save(auction);

        // 3. 사진 엔티티 생성 및 저장
        if (request.getPictures() != null) {
            List<Picture> pictures = request.getPictures().stream()
                    .map(picDto -> Picture.builder()
                            .auction(savedAuction)
                            .imageUrl(picDto.getUrl())
                            .imageKey(picDto.getImageKey())
                            .isMain(picDto.getIsMain())
                            .sortOrder(picDto.getSortOrder())
                            .build())
                    .collect(Collectors.toList());

            pictureRepository.saveAll(pictures);
        }

        return savedAuction.getId();
    }

    /**
     * 메인 페이지: 전체 경매 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AuctionDto.ListResponse> getAuctionList() {
        return auctionRepository.findAll().stream().map(auction -> {
            String mainUrl = auction.getPictures().stream()
                    .filter(Picture::getIsMain)
                    .map(picture -> imageService.createPresignedUrl(picture.getImageKey()))
                    .findFirst()
                    .orElse(null);

            return AuctionDto.ListResponse.builder()
                    .id(auction.getId())
                    .title(auction.getTitle())
                    .currentPrice(auction.getCurrentPrice())
                    .status(auction.getStatus().name())
                    .category(auction.getCategory().name())
                    .mainPictureUrl(mainUrl)
                    .startTime(auction.getStartTime())
                    .endTime(auction.getEndTime())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 상세 페이지: 특정 경매 상세 정보 조회
     */
    @Transactional
    public AuctionDto.DetailResponse getAuctionDetail(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 경매 물품을 찾을 수 없습니다. ID: " + id));

        // 조회수 증가
        auction.increaseViewCount();

        List<AuctionDto.PictureInfo> pictureInfos = auction.getPictures().stream()
                .map(p -> AuctionDto.PictureInfo.builder()
                        .url(imageService.createPresignedUrl(p.getImageKey()))
                        .imageKey(p.getImageKey())
                        .isMain(p.getIsMain())
                        .sortOrder(p.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        // 입찰 기록 (나중에 추가 구현 가능)
        List<AuctionDto.BidInfo> biddingHistory = auction.getBids().stream()
                .map(bid -> AuctionDto.BidInfo.builder()
                        .bidderNickname(bid.getUser().getNickname()) // 입찰자 닉네임
                        .price(bid.getPrice())                      // 입찰 금액
                        .bidTime(bid.getUpdatedAt())                // 입찰 시간
                        .build())
                .sorted((b1, b2) -> b2.getBidTime().compareTo(b1.getBidTime()))
                .collect(Collectors.toList());

        return AuctionDto.DetailResponse.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .description(auction.getDescription())
                .currentPrice(auction.getCurrentPrice())
                .startPrice(auction.getStartPrice())
                .status(auction.getStatus().name())
                .category(auction.getCategory().name())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .viewCount(auction.getViewCount())
                .likeCount(auction.getLikeCount())
                .sellerNickname(auction.getSeller().getNickname())
                .sellerId(auction.getSeller().getId())
                .pictures(pictureInfos)
                .biddingHistory(biddingHistory)
                .build();
    }

    @Transactional
    public AuctionDto.LikeToggleResponse toggleLike(Long auctionId, User user) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 경매 물품을 찾을 수 없습니다. ID: " + auctionId));

        Optional<AuctionLike> auctionLike = auctionLikeRepository.findByUserAndAuction(user, auction);
        boolean isLiked;

        if (auctionLike.isPresent()) {
            auctionLikeRepository.delete(auctionLike.get());
            auction.decreaseLikeCount();
            isLiked = false;
        } else {
            auctionLikeRepository.save(AuctionLike.builder().user(user).auction(auction).build());
            auction.increaseLikeCount();
            isLiked = true;
        }

        return AuctionDto.LikeToggleResponse.builder()
                .auctionId(auctionId)
                .likeCount(auction.getLikeCount())
                .isLiked(isLiked)
                .build();
    }

    /**
     * 메인 페이지 통계 정보 조회
     */
    @Transactional(readOnly = true)
    public AuctionStatsResponse getStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        long totalActive = auctionRepository.countByStatus(AuctionStatus.LIVE);
        long endingSoon = auctionRepository.countByStatusAndEndTimeBetween(AuctionStatus.LIVE, now, oneHourLater);

        return AuctionStatsResponse.builder()
                .totalActive(totalActive)
                .endingSoon(endingSoon)
                .build();
    }
}
