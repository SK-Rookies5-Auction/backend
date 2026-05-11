package com.secureauction.auction.service;

import com.secureauction.auction.domain.User;
import com.secureauction.auction.dto.PasswordUpdateRequest;
import com.secureauction.auction.dto.UserInfoResponse;
import com.secureauction.auction.dto.UserSummaryResponse;
import com.secureauction.auction.dto.UserUpdateRequest;
import com.secureauction.auction.exception.BusinessException;
import com.secureauction.auction.exception.ErrorCode;
import com.secureauction.auction.repository.UserRepository;
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
}
