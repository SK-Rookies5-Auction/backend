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
    private final ImageService imageService;

    // --- 공통 DTO 변환 메서드 ---
    private AuctionDto.MyPageListResponse convertToMyPageListResponse(Auction auction) {
        String mainUrl = auction.getPictures().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsMain()))
                .findFirst()
                .map(p -> imageService.createPresignedUrl(p.getImageKey()))
                .orElse(null);

        return AuctionDto.MyPageListResponse.builder()
                .auctionId(auction.getId())
                .title(auction.getTitle())
                .currentPrice(auction.getCurrentPrice())
                .status(auction.getStatus().name())
                .viewCount(auction.getViewCount())
                .likeCount(auction.getLikeCount())
                .createdAt(auction.getCreatedAt())
                .mainPictureUrl(mainUrl)
                .build();
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getUserSummary(Long userId) {
        return UserSummaryResponse.builder()
                .biddingCount(0)
                .wonCount(0)
                .hostedCount(0)
                .watchlistCount(0)
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
                    .map(this::convertToMyPageListResponse);
        }

        AuctionStatus auctionStatus;
        try {
            auctionStatus = AuctionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return auctionRepository.findBySellerAndStatus(user, auctionStatus, pageable)
                .map(this::convertToMyPageListResponse);
    }

    // 6. 내가 입찰한 경매 목록
    @Transactional(readOnly = true)
    public Page<AuctionDto.MyPageListResponse> getMyBids(User user, Pageable pageable) {
        return bidRepository.findBidAuctionsByUser(user, pageable)
                .map(this::convertToMyPageListResponse);
    }

    // 7. 관심 상품 목록 (수정됨)
    @Transactional(readOnly = true)
    public Page<AuctionDto.MyPageListResponse> getMyWishlist(User user, Pageable pageable) {
        return auctionLikeRepository.findByUser(user, pageable)
                .map(like -> convertToMyPageListResponse(like.getAuction()));
    }
}
