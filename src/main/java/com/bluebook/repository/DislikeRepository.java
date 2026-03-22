package com.bluebook.repository;

import com.bluebook.model.Dislike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DislikeRepository extends JpaRepository<Dislike, Long> {
    Optional<Dislike> findByUserIdAndPostId(Long userId, Long postId);
    long countByPostId(Long postId);
}