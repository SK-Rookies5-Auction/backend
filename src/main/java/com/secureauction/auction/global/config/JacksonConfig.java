package com.secureauction.auction.global.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 시간대 설정 (UTC)
            builder.timeZone(TimeZone.getTimeZone("UTC"));
            
            // LocalDateTime 직렬화: KST -> UTC 변환 후 출력
            builder.serializerByType(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    if (value != null) {
                        // 1. LocalDateTime을 KST 시간으로 간주하여 ZonedDateTime 생성
                        // 2. 이를 UTC 시간대로 변환 (.withZoneSameInstant)
                        // 3. 포맷팅하여 출력
                        String formatted = value.atZone(KST_ZONE)
                                .withZoneSameInstant(ZoneId.of("UTC"))
                                .format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
                        gen.writeString(formatted);
                    }
                }
            });

            // LocalDateTime 역직렬화: UTC -> KST 변환 후 저장
            builder.deserializerByType(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String date = p.getText();
                    if (date != null && !date.isEmpty()) {
                        return ZonedDateTime.parse(date)
                                .withZoneSameInstant(KST_ZONE)
                                .toLocalDateTime();
                    }
                    return null;
                }
            });
        };
    }
}
