package com.secureauction.auction.service;

import com.secureauction.auction.domain.*;
import com.secureauction.auction.dto.AuctionDto;
import com.secureauction.auction.dto.AuctionStatsResponse;
import com.secureauction.auction.global.security.CustomUserDetails;
import com.secureauction.auction.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final PictureRepository pictureRepository;
    private final ImageService imageService;
    private final AuctionLikeRepository auctionLikeRepository;
    private final AuctionInternalService auctionInternalService;

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
     * 상세 페이지: 특정 경매 상세 정보 조회
     * '상태 지연' 방지를 위해 조회 시점에 마감 시간이 지났다면 즉시 종료 처리를 시도함 (Lazy Closure)
     */
    @Transactional
    public AuctionDto.DetailResponse getAuctionDetail(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 경매 물품을 찾을 수 없습니다. ID: " + id));

        // 마감 시간이 지났는데 아직 LIVE 상태라면 즉시 종료 처리 시도
        if (auction.getStatus() == AuctionStatus.LIVE && auction.getEndTime().isBefore(LocalDateTime.now())) {
            try {
                // 프록시를 통한 호출로 REQUIRES_NEW 트랜잭션 보장
                auctionInternalService.closeAuctionIfExpired(auction.getId());
                // 변경된 상태(FINISHED, winner 등)를 반영하기 위해 재조회
                auction = auctionRepository.findById(id).orElse(auction);
            } catch (Exception e) {
                log.error("Failed to perform lazy closure for auction {}: {}", id, e.getMessage());
            }
        }

        // 조회수 증가
        auction.increaseViewCount();

        // 현재 로그인한 사용자의 좋아요 여부 확인
        boolean isLiked = false;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            User currentUser = ((CustomUserDetails) authentication.getPrincipal()).getUser();
            isLiked = auctionLikeRepository.findByUserAndAuction(currentUser, auction).isPresent();
        }

        List<AuctionDto.PictureInfo> pictureInfos = auction.getPictures().stream()
                .map(p -> AuctionDto.PictureInfo.builder()
                        .url(imageService.createPresignedUrl(p.getImageKey()))
                        .imageKey(p.getImageKey())
                        .isMain(p.getIsMain())
                        .sortOrder(p.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        String mainUrl = pictureInfos.stream()
                .filter(AuctionDto.PictureInfo::getIsMain)
                .map(AuctionDto.PictureInfo::getUrl)
                .findFirst()
                .orElse(null);

        // 입찰 기록
        List<AuctionDto.BidInfo> biddingHistory = auction.getBids().stream()
                .map(bid -> AuctionDto.BidInfo.builder()
                        .bidderNickname(bid.getUser().getNickname()) // 입찰자 닉네임
                        .price(bid.getPrice())                      // 입찰 금액
                        .bidTime(bid.getUpdatedAt())                // 입찰 시간
                        .build())
                .sorted((b1, b2) -> {
                    if (b1.getBidTime() == null || b2.getBidTime() == null) return 0;
                    return b2.getBidTime().compareTo(b1.getBidTime());
                })
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
                .isLiked(isLiked)
                .mainPictureUrl(mainUrl)
                .sellerNickname(auction.getSeller().getNickname())
                .sellerId(auction.getSeller().getId())
                .winnerId(auction.getWinner() != null ? auction.getWinner().getId() : null)
                .winnerNickname(auction.getWinner() != null ? auction.getWinner().getNickname() : null)
                .pictures(pictureInfos)
                .biddingHistory(biddingHistory)
                .build();
    }

    /**
     * 메인 페이지: 전체 경매 목록 조회 (필터링 및 정렬 지원)
     */
    @Transactional(readOnly = true)
    public Page<AuctionDto.ListResponse> getAuctionList(
            String category, String q, Long minPrice, Long maxPrice, String sort, Pageable pageable) {
        
        // 현재 로그인 사용자 식별
        User currentUser = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            currentUser = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        }
        final User finalUser = currentUser;

        // 정렬 조건 처리
        Sort sortCondition = switch (sort) {
            case "closing-soon" -> Sort.by(Sort.Direction.ASC, "endTime");
            case "price-low" -> Sort.by(Sort.Direction.ASC, "currentPrice");
            case "price-high" -> Sort.by(Sort.Direction.DESC, "currentPrice");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortCondition);

        // 동적 쿼리 (Specification) 생성
        Specification<Auction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 상태 (기본적으로 LIVE 인 것만 조회)
            predicates.add(cb.equal(root.get("status"), AuctionStatus.LIVE));

            // 2. 카테고리 필터
            if (category != null && !category.isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("category"), Category.valueOf(category)));
                } catch (IllegalArgumentException ignored) {}
            }

            // 3. 검색어 필터 (제목)
            if (q != null && !q.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + q.toLowerCase() + "%"));
            }

            // 4. 가격 범위 필터
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("currentPrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("currentPrice"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 좋아요 여부를 효율적으로 확인하기 위해 현재 유저의 좋아요 목록을 먼저 조회
        List<Long> likedAuctionIds = new ArrayList<>();
        if (finalUser != null) {
            likedAuctionIds = auctionLikeRepository.findByUser(finalUser).stream()
                    .map(like -> like.getAuction().getId())
                    .collect(Collectors.toList());
        }
        final List<Long> finalLikedIds = likedAuctionIds;

        return auctionRepository.findAll(spec, sortedPageable).map(auction -> {
            String mainUrl = auction.getPictures().stream()
                    .filter(Picture::getIsMain)
                    .map(picture -> imageService.createPresignedUrl(picture.getImageKey()))
                    .findFirst()
                    .orElse(null);

            boolean isLiked = finalLikedIds.contains(auction.getId());

            return AuctionDto.ListResponse.builder()
                    .id(auction.getId())
                    .title(auction.getTitle())
                    .currentPrice(auction.getCurrentPrice())
                    .status(auction.getStatus().name())
                    .category(auction.getCategory().name())
                    .mainPictureUrl(mainUrl)
                    .startTime(auction.getStartTime())
                    .endTime(auction.getEndTime())
                    .isLiked(isLiked)
                    .sellerId(auction.getSeller().getId())
                    .build();
        });
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