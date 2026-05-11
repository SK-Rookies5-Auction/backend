package com.secureauction.auction.domain.auctions.service;

import com.secureauction.auction.domain.auctions.dto.AuctionRequestDto;
import com.secureauction.auction.domain.auctions.dto.AuctionResponseDto;
import com.secureauction.auction.domain.auctions.entity.Auction;
import com.secureauction.auction.domain.auctions.entity.AuctionCategory;
import com.secureauction.auction.domain.auctions.entity.AuctionStatus;
import com.secureauction.auction.domain.auctions.entity.Picture;
import com.secureauction.auction.domain.auctions.repository.AuctionRepository;
import com.secureauction.auction.domain.auctions.repository.PictureRepository;
import com.secureauction.auction.domain.user.entity.User;
import com.secureauction.auction.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final PictureRepository pictureRepository;
    private final UserRepository userRepository;

    /**
     * [등록] 경매 상품 등록
     */
    @Transactional
    public Long createAuction(AuctionRequestDto.CreateRequest request) {
        User tempSeller = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("테스트용 유저가 없습니다. DB에 유저를 먼저 넣어주세요!"));
        // 1. Auction 엔티티 생성
        Auction auction = Auction.builder()
                .seller(tempSeller)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(AuctionCategory.valueOf(request.getCategory()))
                .startPrice(request.getStartPrice())
                .currentPrice(request.getStartPrice())
                .startTime(LocalDateTime.now()) // 등록 시점을 시작 시간으로 설정
                .endTime(request.getEndTime())
                .status(AuctionStatus.LIVE)
                .viewCount(0)
                .likeCount(0)
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
    public List<AuctionResponseDto.ListResponse> getAuctionList() {
        return auctionRepository.findAll().stream().map(auction -> {
            // isMain이 true인 사진만 추출
            String mainUrl = auction.getPictures().stream()
                    .filter(Picture::getIsMain)
                    .map(Picture::getImageUrl)
                    .findFirst()
                    .orElse(null);

            return AuctionResponseDto.ListResponse.builder()
                    .id(auction.getId())
                    .title(auction.getTitle())
                    .currentPrice(auction.getCurrentPrice())
                    .status(auction.getStatus())
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
    @Transactional(readOnly = true)
    public AuctionResponseDto.DetailResponse getAuctionDetail(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 경매 물품을 찾을 수 없습니다. ID: " + id));

        // 전체 사진 URL 리스트 추출
        List<AuctionResponseDto.PictureInfo> pictureInfos = auction.getPictures().stream()
                .map(p -> AuctionResponseDto.PictureInfo.builder()
                        .url(p.getImageUrl())
                        .isMain(p.getIsMain())
                        .build())
                .collect(Collectors.toList());

        // 입찰 기록
        List<AuctionResponseDto.BidInfo> biddingHistory = new ArrayList<>();

        return AuctionResponseDto.DetailResponse.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .description(auction.getDescription())
                .currentPrice(auction.getCurrentPrice())
                .startPrice(auction.getStartPrice())
                .status(auction.getStatus())
                .category(auction.getCategory().name())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .viewCount(auction.getViewCount())
                .likeCount(auction.getLikeCount())
                .sellerNickname(auction.getSeller().getNickname())
                .pictures(pictureInfos)
                .biddingHistory(biddingHistory)
                .build();
    }
}
