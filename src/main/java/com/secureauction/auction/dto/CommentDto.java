package com.secureauction.auction.dto;

import com.secureauction.auction.domain.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommentDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String content;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String content;
        private String nickname;
        private LocalDateTime createdAt;
        private List<Response> children;

        public static Response from(Comment comment) {
            return Response.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .nickname(comment.getUser().getNickname())
                    .createdAt(comment.getCreatedAt())
                    .children(comment.getChildren().stream()
                            .map(Response::from)
                            .collect(Collectors.toList()))
                    .build();
        }
    }
}
