package com.secureauction.auction.controller;

import com.secureauction.auction.dto.ApiResponse;
import com.secureauction.auction.dto.CommentDto;
import com.secureauction.auction.global.security.CustomUserDetails;
import com.secureauction.auction.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions/{auctionId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글(문의) 등록
     */
    @PostMapping
    public ApiResponse<Long> createComment(
            @PathVariable Long auctionId,
            @RequestBody CommentDto.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long commentId = commentService.createComment(auctionId, request, userDetails.getUser());
        return ApiResponse.success(commentId, "댓글이 성공적으로 등록되었습니다.");
    }

    /**
     * 문의 답변 등록 (추가된 부분)
     */
    @PostMapping("/{commentId}/answers")
    public ApiResponse<Long> createAnswer(
            @PathVariable Long auctionId,
            @PathVariable Long commentId,
            @RequestBody CommentDto.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long answerId = commentService.createAnswer(auctionId, commentId, request, userDetails.getUser());
        return ApiResponse.success(answerId, "답변이 성공적으로 등록되었습니다.");
    }

    /**
     * 특정 경매의 댓글 목록 조회
     */
    @GetMapping
    public ApiResponse<List<CommentDto.Response>> getComments(@PathVariable Long auctionId) {
        List<CommentDto.Response> response = commentService.getComments(auctionId);
        return ApiResponse.success(response, "댓글 목록을 성공적으로 조회했습니다.");
    }
}
