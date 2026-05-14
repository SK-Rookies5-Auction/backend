package com.secureauction.auction.service;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.Comment;
import com.secureauction.auction.domain.NotificationType;
import com.secureauction.auction.domain.User;
import com.secureauction.auction.dto.CommentDto;
import com.secureauction.auction.exception.BusinessException;
import com.secureauction.auction.exception.ErrorCode;
import com.secureauction.auction.repository.AuctionRepository;
import com.secureauction.auction.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final AuctionRepository auctionRepository;
    private final NotificationService notificationService;

    @Transactional
    public Long createComment(Long auctionId, CommentDto.CreateRequest request, User user) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 경매가 없습니다."));

        Comment comment = Comment.builder()
                .auction(auction)
                .user(user)
                .content(request.getContent())
                .build();

        Comment savedComment = commentRepository.save(comment);

        // 2. [알림 로직 추가] 판매자에게 알림 보내기
        if (!auction.getSeller().getId().equals(user.getId())) {
            notificationService.createNotification(
                    auction.getSeller(), // 수신자: 판매자
                    NotificationType.NEW_QUESTION, // 타입: 댓글 알림
                    String.format("[%s] 상품에 새로운 문의가 등록되었습니다.", auction.getTitle()), // 내용
                    "/product/" + auctionId // 클릭 시 이동할 URL
            );
        }

        return savedComment.getId();
    }

    @Transactional
    public Long createAnswer(Long auctionId, Long parentId, CommentDto.CreateRequest request, User user) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 1. 부모 질문 조회
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. 답변 엔티티 생성 (parent 설정)
        Comment answer = Comment.builder()
                .auction(auction)
                .user(user)
                .content(request.getContent())
                .parent(parentComment) // 부모 댓글 연결
                .build();

        Comment savedAnswer = commentRepository.save(answer);

        // 3. [알림 로직] 질문자에게 답변 알림 전송 (NEW_ANSWER)
        // 답변자가 질문자 본인이 아닐 때만 발송
        if (!parentComment.getUser().getId().equals(user.getId())) {
            notificationService.createNotification(
                    parentComment.getUser(), // 수신자: 질문자
                    NotificationType.NEW_ANSWER, // 타입: 답변 등록
                    String.format("[답변 완료] 문의하신 '%s' 상품에 답변이 등록되었습니다.", auction.getTitle()),
                    "/product/" + auctionId
            );
        }

        return savedAnswer.getId();
    }

    @Transactional(readOnly = true)
    public List<CommentDto.Response> getComments(Long auctionId) {
        return commentRepository.findByAuctionIdAndParentIsNull(auctionId).stream()
                .map(CommentDto.Response::from)
                .collect(Collectors.toList());
    }
}
