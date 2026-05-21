package com.example.ecommerce.repository;

import com.example.ecommerce.entity.BrowseLog;
import com.example.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface BrowseLogRepository extends JpaRepository<BrowseLog, Long> {

    List<BrowseLog> findByUser(User user);

    List<BrowseLog> findByUserOrderByBrowseTimeDesc(User user);

    @Query("SELECT b.category, COUNT(b) FROM BrowseLog b " +
           "WHERE b.user = :user GROUP BY b.category ORDER BY COUNT(b) DESC")
    List<Object[]> findUserCategoryPreferences(@Param("user") User user);

    @Query("SELECT b.category, COUNT(b) FROM BrowseLog b " +
           "WHERE b.browseTime BETWEEN :start AND :end GROUP BY b.category ORDER BY COUNT(b) DESC")
    List<Object[]> findCategoryBrowseCount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT b.user, COUNT(b) FROM BrowseLog b " +
           "WHERE b.browseTime BETWEEN :start AND :end GROUP BY b.user ORDER BY COUNT(b) DESC")
    List<Object[]> findMostActiveUsers(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByBrowseTimeBetween(LocalDateTime start, LocalDateTime end);
}
