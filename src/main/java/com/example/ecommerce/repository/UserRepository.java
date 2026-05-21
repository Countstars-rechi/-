package com.example.ecommerce.repository;

import com.example.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByRole(String role);

    @Query("SELECT u.region, COUNT(u) FROM User u GROUP BY u.region")
    List<Object[]> countUsersByRegion();

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = :enabled")
    List<User> findByRoleAndEnabled(@Param("role") String role, @Param("enabled") boolean enabled);

    long countByRole(String role);
}
