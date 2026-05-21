package com.example.ecommerce.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "browse_logs")
public class BrowseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;        // 浏览的商品（可为空，表示浏览首页等）

    private String category;        // 商品类别

    private Integer stayDuration;   // 停留时长（秒）

    @Column(nullable = false)
    private String ipAddress;       // IP地址

    @Column(updatable = false)
    private LocalDateTime browseTime;   // 浏览时间

    @PrePersist
    protected void onCreate() {
        browseTime = LocalDateTime.now();
    }
}
