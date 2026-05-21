package com.example.ecommerce.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private Integer stock;          // 库存数量

    private String category;        // 商品类别（如：手机、平板、耳机等）

    private String imageUrl;

    private boolean enabled = true; // 是否上架
}
