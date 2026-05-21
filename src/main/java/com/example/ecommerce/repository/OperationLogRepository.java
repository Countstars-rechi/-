package com.example.ecommerce.repository;

import com.example.ecommerce.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    List<OperationLog> findByUsernameOrderByOperationTimeDesc(String username);

    List<OperationLog> findAllByOrderByOperationTimeDesc();

    @Query("SELECT o.username, COUNT(o) FROM OperationLog o " +
           "WHERE o.operationTime BETWEEN :start AND :end GROUP BY o.username ORDER BY COUNT(o) DESC")
    List<Object[]> findMostActiveOperators(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o FROM OperationLog o WHERE o.operationTime BETWEEN :start AND :end ORDER BY o.operationTime DESC")
    List<OperationLog> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
