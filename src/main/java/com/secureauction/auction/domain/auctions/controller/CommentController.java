package com.secureauction.auction.domain.auctions.controller;

import com.secureauction.auction.domain.auctions.dto.CommentRequestDto;
import com.secureauction.auction.domain.auctions.dto.CommentResponseDto;
import com.secureauction.auction.domain.auctions.service.CommentService;
import com.secureauction.auction.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    /**
     * [등록] 특정 경매에 댓글(문의) 등록
     */
    @PostMapping("/{auctionId}/comments")
    public ApiResponse<Long> createComment(
            @PathVariable Long auctionId,
            @RequestBody CommentRequestDto.Create request) {

        Long commentId = commentService.createComment(auctionId, request);
        return ApiResponse.success(commentId, "댓글이 성공적으로 등록되었습니다.");
    }

    /**
     * [조회] 특정 경매의 모든 댓글(문의) 목록 조회
     */
    @GetMapping("/{auctionId}/comments")
    public ApiResponse<List<CommentResponseDto>> getComments(@PathVariable Long auctionId) {

        List<CommentResponseDto> responses = commentService.getComments(auctionId);
        return ApiResponse.success(responses, "댓글 목록을 성공적으로 조회했습니다.");
    }
}
