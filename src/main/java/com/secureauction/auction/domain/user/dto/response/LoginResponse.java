package com.secureauction.auction.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private UserDto user;

    @Getter
    @Builder
    public static class UserDto {
        private Long id;
        private String role;
        private String nickname;
        private String email;
    }
}