package com.secureauction.auction.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @JsonProperty("login_id")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;
}