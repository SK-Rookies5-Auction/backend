package com.secureauction.auction.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cloud.aws.s3")
public class S3Properties {
    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;
    private String baseDirectory = "auction-images";
    private long presignedUrlExpirationMinutes = 10;
    private long maxUploadSize = 10 * 1024 * 1024;

    public boolean hasStaticCredentials() {
        return StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey);
    }
}
