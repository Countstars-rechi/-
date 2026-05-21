package com.example.ecommerce.service;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在：" + username));

        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("账号已被禁用");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(() -> "ROLE_" + user.getRole())
        );
    }

    // 用户注册
    public User registerUser(String username, String password, String fullName,
                              String email, String phone, String region) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRegion(region);
        user.setRole("CUSTOMER"); // 默认注册为普通用户
        return userRepository.save(user);
    }

    // 获取当前用户
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    }

    // 保存用户
    public User saveUser(User user) {
        if (user.getId() == null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    // 更新最后登录信息
    public void updateLoginInfo(String username, String ip) {
        User user = getCurrentUser(username);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userRepository.save(user);
    }

    // 管理员：获取所有销售人员
    public List<User> getAllSales() {
        return userRepository.findByRole("SALES");
    }

    // 管理员：获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 管理员：创建销售人员账号
    public User createSalesUser(String username, String password, String fullName) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole("SALES");
        return userRepository.save(user);
    }

    // 管理员：重置密码
    public void resetPassword(String username, String newPassword) {
        User user = getCurrentUser(username);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // 管理员：启用/禁用账号
    public void setUserEnabled(String username, boolean enabled) {
        User user = getCurrentUser(username);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    // 获取用户地域分布
    public List<Object[]> getUserRegionDistribution() {
        return userRepository.countUsersByRegion();
    }

    // 统计各角色用户数
    public long countByRole(String role) {
        return userRepository.countByRole(role);
    }
}
