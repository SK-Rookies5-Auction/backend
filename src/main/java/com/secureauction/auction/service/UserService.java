package com.secureauction.auction.service;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.AuctionStatus;
import com.secureauction.auction.domain.User;
import com.secureauction.auction.dto.*;
import com.secureauction.auction.exception.BusinessException;
import com.secureauction.auction.exception.ErrorCode;
import com.secureauction.auction.repository.AuctionLikeRepository;
import com.secureauction.auction.repository.AuctionRepository;
import com.secureauction.auction.repository.BidRepository;
import com.secureauction.auction.repository.PaymentRepository;
import com.secureauction.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuctionLikeRepository auctionLikeRepository;
    private final AuctionRepository auctionRepository; // 추가됨
    private final BidRepository bidRepository; // 추가됨
    private final PaymentRepository paymentRepository;
    private final ImageService imageService;

    // --- 공통 DTO 변환 메서드 ---
    private AuctionDto.MyPageListResponse convertToMyBidListResponse(Auction auction, User user) {
        Long highestBidPrice = bidRepository.findHighestPriceByAuction(auction);
        Long myHighestBidPrice = bidRepository.findHighestPriceByAuctionAndUser(auction, user);
        
        // Null safety for primitive Long unboxing if needed, though DTO usually accepts Long
        long current = highestBidPrice != null ? highestBidPrice : auction.getCurrentPrice();
        
        return convertToMyPageListResponse(auction, current, myHighestBidPrice, user);
    }

    private AuctionDto.MyPageListResponse convertToMyPageListResponse(Auction auction, Long currentPrice, Long myPrice, User currentUser) {
        String mainUrl = auction.getPictures().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsMain()))
                .findFirst()
                .map(p -> imageService.createPresignedUrl(p.getImageKey()))
                .orElse(null);

        Long finalPrice = null;
        if (auction.getStatus() != AuctionStatus.LIVE && auction.getStatus() != AuctionStatus.READY) {
            finalPrice = paymentRepository.findByAuction(auction)
                    .map(com.secureauction.auction.domain.Payment::getFinalPrice)
                    .orElse(null);
        }

        // 상태 계산 로직 (WON, OUTBID, SOLD 등)
        String statusStr = auction.getStatus().name();
        if (currentUser != null) {
            // 내가 입찰자 혹은 위너인 경우
            if (auction.getStatus() == AuctionStatus.FINISHED) {
                if (auction.getWinner() != null && currentUser.getId().equals(auction.getWinner().getId())) {
                    statusStr = "WON";
                } else if (myPrice != null) {
                    statusStr = "LOST";
                }
            } else if (auction.getStatus() == AuctionStatus.LIVE && myPrice != null) {
                if (myPrice < currentPrice) {
                    statusStr = "OUTBID";
                }
            }
            // 내가 판매자인 경우
            if (currentUser.getId().equals(auction.getSeller().getId()) && auction.getStatus() == AuctionStatus.FINISHED) {
                if (auction.getWinner() != null) {
                    statusStr = "SOLD";
                } else {
                    statusStr = "FINISHED";
                }
            }
        }

        return AuctionDto.MyPageListResponse.builder()
                .auctionId(auction.getId())
                .title(auction.getTitle())
                .currentPrice(currentPrice)
                .myPrice(myPrice)
                .finalPrice(finalPrice)
                .status(statusStr)
                .viewCount(auction.getViewCount())
                .likeCount(auction.getLikeCount())
                .createdAt(auction.getCreatedAt())
                .mainPictureUrl(mainUrl)
                .build();
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getUserSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        return UserSummaryResponse.builder()
                .biddingCount(Math.toIntExact(bidRepository.countDistinctAuctionsByUser(user)))
                .wonCount(Math.toIntExact(auctionRepository.countByWinner(user)))
                .hostedCount(Math.toIntExact(auctionRepository.countBySeller(user)))
                .watchlistCount(Math.toIntExact(auctionLikeRepository.countByUser(user)))
                .build();
    }

    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        return UserInfoResponse.from(user);
    }

    @Transactional
    public void updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!user.getNickname().equals(request.getNickname()) && userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }

        user.updateInfo(request.getNickname());
    }

    // 5. 내가 등록한 경매 목록
    @Transactional(readOnly = true)
    public Page<AuctionDto.MyPageListResponse> getMyAuctions(User user, String status, Pageable pageable) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return auctionRepository.findBySeller(user, pageable)
                    .map(auction -> convertToMyPageListResponse(auction, auction.getCurrentPrice(), null, user));
        }

        if ("SOLD".equalsIgnoreCase(status)) {
            return auctionRepository.findBySellerAndStatusAndWinnerIsNotNull(user, AuctionStatus.FINISHED, pageable)
                    .map(auction -> convertToMyPageListResponse(auction, auction.getCurrentPrice(), null, user));
        }

        if ("FINISHED".equalsIgnoreCase(status)) {
            return auctionRepository.findBySellerAndStatusAndWinnerIsNull(user, AuctionStatus.FINISHED, pageable)
                    .map(auction -> convertToMyPageListResponse(auction, auction.getCurrentPrice(), null, user));
        }

        AuctionStatus auctionStatus;
        try {
            auctionStatus = AuctionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return auctionRepository.findBySellerAndStatus(user, auctionStatus, pageable)
                .map(auction -> convertToMyPageListResponse(auction, auction.getCurrentPrice(), null, user));
    }

    // 6. 내가 입찰한 경매 목록
    @Transactional(readOnly = true)
    public Page<AuctionDto.MyPageListResponse> getMyBids(User user, String status, Pageable pageable) {
        Page<Auction> auctions;
        
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            auctions = bidRepository.findBidAuctionsByUser(user, pageable);
        } else if ("WON".equalsIgnoreCase(status)) {
            auctions = auctionRepository.findByWinnerAndStatus(user, AuctionStatus.FINISHED, pageable);
        } else if ("LOST".equalsIgnoreCase(status)) {
            auctions = bidRepository.findLostAuctionsByUser(user, pageable);
        } else if ("PAID".equalsIgnoreCase(status) || "SHIPPING".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
            auctions = auctionRepository.findByWinnerAndStatus(user, AuctionStatus.valueOf(status.toUpperCase()), pageable);
        } else if ("OUTBID".equalsIgnoreCase(status)) {
            auctions = bidRepository.findOutbidAuctionsByUser(user, pageable);
        } else {
            AuctionStatus auctionStatus;
            try {
                auctionStatus = AuctionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            auctions = bidRepository.findBidAuctionsByUserAndStatus(user, auctionStatus, pageable);
        }

        return auctions.map(auction -> convertToMyBidListResponse(auction, user));
    }

    // 7. 관심 상품 목록 (수정됨)
    @Transactional(readOnly = true)
    public Page<AuctionDto.MyPageListResponse> getMyWishlist(User user, Pageable pageable) {
        return auctionLikeRepository.findByUser(user, pageable)
                .map(like -> convertToMyPageListResponse(like.getAuction(), like.getAuction().getCurrentPrice(), null, user));
    }
}
