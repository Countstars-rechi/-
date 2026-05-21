package com.example.ecommerce.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;

    private String email;       // 邮箱（用于邮件确认）

    private String phone;       // 电话

    private String region;      // 地域（用于用户画像）

    @Column(nullable = false)
    private String role;        // 角色: CUSTOMER, SALES, ADMIN

    @Column(updatable = false)
    private LocalDateTime createdAt;    // 注册时间

    private LocalDateTime lastLoginAt;  // 最后登录时间

    private String lastLoginIp;         // 最后登录IP

    private boolean enabled = true;     // 账号是否启用

    // 关联购物车
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<CartItem> cartItems;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
