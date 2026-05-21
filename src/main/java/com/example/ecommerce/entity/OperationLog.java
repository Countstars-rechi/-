package com.example.ecommerce.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "operation_logs")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;        // 操作人用户名

    @Column(nullable = false)
    private String role;            // 操作人角色

    @Column(nullable = false)
    private String operation;       // 操作内容（如：修改商品价格、删除用户等）

    @Column(nullable = false)
    private String ipAddress;       // 操作IP

    @Column(updatable = false)
    private LocalDateTime operationTime;    // 操作时间

    @PrePersist
    protected void onCreate() {
        operationTime = LocalDateTime.now();
    }
}
