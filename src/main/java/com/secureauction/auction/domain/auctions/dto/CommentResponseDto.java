package com.secureauction.auction.domain.auctions.dto;

import com.secureauction.auction.domain.auctions.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponseDto {
    private Long id;
    private Long userId;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;

    public static CommentResponseDto from(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .nickname(comment.getUser().getNickname())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
