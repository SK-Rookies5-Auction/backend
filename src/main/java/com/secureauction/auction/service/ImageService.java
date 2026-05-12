package com.secureauction.auction.service;

import com.secureauction.auction.config.S3Properties;
import com.secureauction.auction.dto.ImageDto;
import com.secureauction.auction.exception.BusinessException;
import com.secureauction.auction.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    public ImageDto.UploadResponse upload(MultipartFile file) {
        validateImage(file);

        String imageKey = createImageKey(file.getOriginalFilename());
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(imageKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException | SdkException e) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED, e);
        }

        return ImageDto.UploadResponse.builder()
                .imageUrl(toS3Url(imageKey))
                .imageKey(imageKey)
                .presignedUrl(createPresignedUrl(imageKey))
                .build();
    }

    public String createPresignedUrl(String imageKey) {
        if (!StringUtils.hasText(imageKey)) {
            return null;
        }
        validateS3BucketConfigured();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(imageKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(properties.getPresignedUrlExpirationMinutes()))
                .getObjectRequest(getObjectRequest)
                .build();

        try {
            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (SdkException e) {
            throw new BusinessException(ErrorCode.IMAGE_PRESIGNED_URL_FAILED, e);
        }
    }

    public void delete(String imageKey) {
        if (!StringUtils.hasText(imageKey)) {
            return;
        }
        validateS3BucketConfigured();

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(imageKey)
                .build();

        try {
            s3Client.deleteObject(request);
        } catch (SdkException e) {
            throw new BusinessException(ErrorCode.IMAGE_DELETE_FAILED, e);
        }
    }

    private void validateImage(MultipartFile file) {
        validateS3BucketConfigured();
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_FILE_REQUIRED);
        }
        if (file.getSize() > properties.getMaxUploadSize()) {
            throw new BusinessException(ErrorCode.IMAGE_FILE_SIZE_EXCEEDED);
        }

        // 1. 헤더 기반 1차 검증
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE_TYPE);
        }

        // 2. 확장자 기반 2차 검증 (화이트리스트 방식)
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE_TYPE);
        }

        String extension = StringUtils.getFilenameExtension(originalFilename);
        // NPE 방지: 확장자가 아예 없는 파일인 경우 튕겨냄
        if (!StringUtils.hasText(extension)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE_TYPE);
        }

        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif", "webp");
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE_TYPE);
        }
    }

    private void validateS3BucketConfigured() {
        if (!StringUtils.hasText(properties.getBucket())) {
            throw new BusinessException(ErrorCode.S3_BUCKET_NOT_CONFIGURED);
        }
    }

    private String createImageKey(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String filename = UUID.randomUUID().toString();
        if (StringUtils.hasText(extension)) {
            filename += "." + extension.toLowerCase();
        }

        LocalDate now = LocalDate.now();
        return "%s/%d/%02d/%02d/%s".formatted(
                trimSlashes(properties.getBaseDirectory()),
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                filename
        );
    }

    private String toS3Url(String imageKey) {
        return "s3://%s/%s".formatted(properties.getBucket(), imageKey);
    }

    private String trimSlashes(String value) {
        if (!StringUtils.hasText(value)) {
            return "auction-images";
        }
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
