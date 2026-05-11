package com.secureauction.auction.dto;

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
        private String nickname;
        private String role;
        private String email;
    }
}
