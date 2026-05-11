package com.secureauction.auction.domain.user.dto.response;

import com.secureauction.auction.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
    private Long Id;
    private String email;
    private String nickname;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .Id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}