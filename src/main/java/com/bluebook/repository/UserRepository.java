package com.bluebook.repository;

import com.bluebook.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByPhone(String phone);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
    
    List<User> findByIsAdminFalse();
    
    List<User> findByIsAdminTrue();
    
    List<User> findByIsAdminFalseOrderByFameDesc();
    
    List<User> findByIsAdminFalseOrderBySearchCountDesc();
    
    List<User> findByIsAdminFalseOrderByDailyFameGrowthDesc();
    
    List<User> findByIsAdminFalseOrderByWeeklyFameGrowthDesc();
    
    List<User> findByIsAdminFalseOrderByMonthlyFameGrowthDesc();
    
    List<User> findByIsAdminFalseOrderByYearlyFameGrowthDesc();
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false ORDER BY u.fame DESC")
    Page<User> findFameRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false ORDER BY u.searchCount DESC")
    Page<User> findSearchRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false ORDER BY u.dailyFameGrowth DESC")
    Page<User> findDailyFameGrowthRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false ORDER BY u.weeklyFameGrowth DESC")
    Page<User> findWeeklyFameGrowthRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false ORDER BY u.monthlyFameGrowth DESC")
    Page<User> findMonthlyFameGrowthRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false ORDER BY u.yearlyFameGrowth DESC")
    Page<User> findYearlyFameGrowthRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.fansCount > 0 ORDER BY u.fansCount DESC")
    Page<User> findFansRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.likesReceived > 0 ORDER BY u.likesReceived DESC")
    Page<User> findLikesRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.postCount > 0 ORDER BY u.postCount DESC")
    Page<User> findPostsRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.collectsReceived > 0 ORDER BY u.collectsReceived DESC")
    Page<User> findCollectsRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.foodSearchCount > 0 ORDER BY u.foodSearchCount DESC")
    Page<User> findFoodSearchRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.foodFame > 0 ORDER BY u.foodFame DESC")
    Page<User> findFoodFameRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.travelSearchCount > 0 ORDER BY u.travelSearchCount DESC")
    Page<User> findTravelSearchRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.travelFame > 0 ORDER BY u.travelFame DESC")
    Page<User> findTravelFameRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.digitalSearchCount > 0 ORDER BY u.digitalSearchCount DESC")
    Page<User> findDigitalSearchRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.digitalFame > 0 ORDER BY u.digitalFame DESC")
    Page<User> findDigitalFameRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.fashionSearchCount > 0 ORDER BY u.fashionSearchCount DESC")
    Page<User> findFashionSearchRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.fashionFame > 0 ORDER BY u.fashionFame DESC")
    Page<User> findFashionFameRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.homeSearchCount > 0 ORDER BY u.homeSearchCount DESC")
    Page<User> findHomeSearchRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.homeFame > 0 ORDER BY u.homeFame DESC")
    Page<User> findHomeFameRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.beautySearchCount > 0 ORDER BY u.beautySearchCount DESC")
    Page<User> findBeautySearchRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.beautyFame > 0 ORDER BY u.beautyFame DESC")
    Page<User> findBeautyFameRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.fitnessSearchCount > 0 ORDER BY u.fitnessSearchCount DESC")
    Page<User> findFitnessSearchRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.fitnessFame > 0 ORDER BY u.fitnessFame DESC")
    Page<User> findFitnessFameRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.otherSearchCount > 0 ORDER BY u.otherSearchCount DESC")
    Page<User> findOtherSearchRank(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isAdmin = false AND u.otherFame > 0 ORDER BY u.otherFame DESC")
    Page<User> findOtherFameRank(Pageable pageable);
    
    default Page<User> findCategorySearchRank(String category, Pageable pageable) {
        switch (category) {
            case "food": return findFoodSearchRank(pageable);
            case "travel": return findTravelSearchRank(pageable);
            case "digital": return findDigitalSearchRank(pageable);
            case "fashion": return findFashionSearchRank(pageable);
            case "home": return findHomeSearchRank(pageable);
            case "beauty": return findBeautySearchRank(pageable);
            case "fitness": return findFitnessSearchRank(pageable);
            case "other": return findOtherSearchRank(pageable);
            default: return Page.empty(pageable);
        }
    }
    
    default Page<User> findCategoryFameRank(String category, Pageable pageable) {
        switch (category) {
            case "food": return findFoodFameRank(pageable);
            case "travel": return findTravelFameRank(pageable);
            case "digital": return findDigitalFameRank(pageable);
            case "fashion": return findFashionFameRank(pageable);
            case "home": return findHomeFameRank(pageable);
            case "beauty": return findBeautyFameRank(pageable);
            case "fitness": return findFitnessFameRank(pageable);
            case "other": return findOtherFameRank(pageable);
            default: return Page.empty(pageable);
        }
    }
}