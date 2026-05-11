package com.secureauction.auction.domain.auctions.service;

import com.secureauction.auction.domain.auctions.dto.CommentRequestDto;
import com.secureauction.auction.domain.auctions.dto.CommentResponseDto;
import com.secureauction.auction.domain.auctions.entity.Auction;
import com.secureauction.auction.domain.auctions.entity.Comment;
import com.secureauction.auction.domain.auctions.repository.AuctionRepository;
import com.secureauction.auction.domain.auctions.repository.CommentRepository;
import com.secureauction.auction.domain.user.entity.User;
import com.secureauction.auction.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public Long createComment(Long auctionId, CommentRequestDto.Create request) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 경매가 없습니다."));

        User user = userRepository.findById(1L).get(); // 임시 1번 유저

        Comment comment = Comment.builder()
                .auction(auction)
                .user(user)
                .content(request.getContent())
                .build();

        return commentRepository.save(comment).getId();
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long auctionId) {
        return commentRepository.findByAuctionId(auctionId).stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
    }
}
