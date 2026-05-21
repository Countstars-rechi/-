package com.example.ecommerce.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNo;     // 订单号

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;          // 下单用户

    @Column(nullable = false)
    private BigDecimal totalAmount; // 订单总金额

    @Column(nullable = false)
    private String status;      // 订单状态: PENDING, PAID, SHIPPED, COMPLETED, CANCELLED

    private String shippingAddress; // 收货地址

    private String receiverName;    // 收货人

    private String receiverPhone;   // 收货人电话

    @Column(updatable = false)
    private LocalDateTime createdAt;    // 下单时间

    private LocalDateTime paidAt;       // 付款时间

    // 订单项
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
