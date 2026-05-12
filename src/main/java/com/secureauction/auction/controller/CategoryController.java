package com.secureauction.auction.controller;

import com.secureauction.auction.domain.Category;
import com.secureauction.auction.dto.ApiResponse;
import com.secureauction.auction.dto.CategoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    @GetMapping
    public ApiResponse<List<CategoryDto>> getCategories() {
        List<CategoryDto> categories = Arrays.stream(Category.values())
                .map(category -> CategoryDto.builder()
                        .name(category.getValue())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success(categories, "카테고리 목록 조회 성공");
    }
}