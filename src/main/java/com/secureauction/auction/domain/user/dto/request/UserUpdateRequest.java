package com.secureauction.auction.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;
}