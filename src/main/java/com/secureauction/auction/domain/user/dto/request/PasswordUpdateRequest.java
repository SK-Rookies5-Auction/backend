package com.secureauction.auction.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordUpdateRequest {
    @NotBlank
    @JsonProperty("current_password") // JSON 요청 매핑
    private String currentPassword;

    @NotBlank
    @JsonProperty("new_password")
    private String newPassword;

    @NotBlank
    @JsonProperty("confirm_password")
    private String confirmPassword;
}