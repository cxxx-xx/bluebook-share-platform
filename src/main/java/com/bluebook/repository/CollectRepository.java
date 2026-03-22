package com.bluebook.repository;

import com.bluebook.model.Collect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectRepository extends JpaRepository<Collect, Long> {
    Optional<Collect> findByUserIdAndPostId(Long userId, Long postId);
    long countByPostId(Long postId);
    List<Collect> findByUserId(Long userId);
}