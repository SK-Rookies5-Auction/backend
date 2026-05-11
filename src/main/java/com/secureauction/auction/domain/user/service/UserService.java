package com.secureauction.auction.domain.user.service;

import com.secureauction.auction.domain.user.dto.request.PasswordUpdateRequest;
import com.secureauction.auction.domain.user.dto.request.UserUpdateRequest;
import com.secureauction.auction.domain.user.dto.response.UserInfoResponse;
import com.secureauction.auction.domain.user.dto.response.UserSummaryResponse;
import com.secureauction.auction.domain.User;
import com.secureauction.auction.domain.user.repository.UserRepository;
import com.secureauction.auction.exception.BusinessException;
import com.secureauction.auction.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserSummaryResponse getUserSummary(Long userId) {
        // TODO: 다른 팀원들이 AuctionRepository, BidRepository를 만들면
        // 여기서 의존성 주입을 받아 count 쿼리를 실행하여 실제 데이터를 채워 넣습니다.
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

        // 닉네임 중복 검사 (본인 닉네임과 다를 경우에만)
        if (!user.getNickname().equals(request.getNickname()) && userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }

        user.updateInfo(request.getNickname());
    }
}