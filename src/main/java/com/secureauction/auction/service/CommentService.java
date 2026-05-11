package com.secureauction.auction.service;

import com.secureauction.auction.domain.Auction;
import com.secureauction.auction.domain.Comment;
import com.secureauction.auction.domain.User;
import com.secureauction.auction.dto.CommentDto;
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

    @Transactional
    public Long createComment(Long auctionId, CommentDto.CreateRequest request, User user) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 경매가 없습니다."));

        Comment comment = Comment.builder()
                .auction(auction)
                .user(user)
                .content(request.getContent())
                .build();

        return commentRepository.save(comment).getId();
    }

    @Transactional(readOnly = true)
    public List<CommentDto.Response> getComments(Long auctionId) {
        return commentRepository.findByAuctionId(auctionId).stream()
                .map(CommentDto.Response::from)
                .collect(Collectors.toList());
    }
}
