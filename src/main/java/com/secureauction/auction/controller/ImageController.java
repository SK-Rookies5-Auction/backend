package com.secureauction.auction.controller;

import com.secureauction.auction.dto.ApiResponse;
import com.secureauction.auction.dto.ImageDto;
import com.secureauction.auction.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;
    @PostMapping
    public ApiResponse<ImageDto.UploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(imageService.upload(file), "이미지가 업로드되었습니다.");
    }
}
