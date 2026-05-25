package com.example.ecommerce.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "browse_logs")
public class BrowseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String category;

    private Integer stayDuration;

    @Column(nullable = false)
    private String ipAddress;

    @Column(updatable = false)
    private LocalDateTime browseTime;

    @PrePersist
    protected void onCreate() {
        browseTime = LocalDateTime.now();
    }
}
