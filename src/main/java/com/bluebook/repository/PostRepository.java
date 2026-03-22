package com.bluebook.repository;

import com.bluebook.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);
    
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<Post> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword% ORDER BY p.createdAt DESC")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Post p ORDER BY p.likesCount DESC, p.createdAt DESC")
    Page<Post> findHotPosts(Pageable pageable);
    
    @Query("SELECT p FROM Post p ORDER BY p.collectsCount DESC, p.createdAt DESC")
    Page<Post> findRecommendedPosts(Pageable pageable);
}