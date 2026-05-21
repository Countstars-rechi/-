package com.example.ecommerce.repository;

import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi.product, SUM(oi.quantity) as totalQty FROM OrderItem oi " +
           "JOIN oi.order o WHERE o.createdAt BETWEEN :start AND :end " +
           "AND (o.status = 'PAID' OR o.status = 'COMPLETED') " +
           "GROUP BY oi.product ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT oi.product, SUM(oi.quantity) as totalQty FROM OrderItem oi " +
           "JOIN oi.order o WHERE (o.status = 'PAID' OR o.status = 'COMPLETED') " +
           "GROUP BY oi.product ORDER BY totalQty DESC")
    List<Object[]> findAllTimeTopSellingProducts();

    @Query("SELECT oi.product.category, SUM(oi.quantity) FROM OrderItem oi " +
           "JOIN oi.order o WHERE o.createdAt BETWEEN :start AND :end " +
           "AND (o.status = 'PAID' OR o.status = 'COMPLETED') " +
           "GROUP BY oi.product.category ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findSalesByCategory(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
