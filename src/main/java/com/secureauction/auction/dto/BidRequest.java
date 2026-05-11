package com.secureauction.auction.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BidRequest {
    @NotNull(message = "입찰 가격은 필수입니다.")
    @Min(value = 1, message = "입찰 가격은 0보다 커야 합니다.")
    private Long price;
}
