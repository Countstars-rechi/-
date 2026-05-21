package com.example.ecommerce.service;

import com.example.ecommerce.entity.*;
import com.example.ecommerce.repository.BrowseLogRepository;
import com.example.ecommerce.repository.OperationLogRepository;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class LogService {

    private final BrowseLogRepository browseLogRepository;
    private final OperationLogRepository operationLogRepository;

    public LogService(BrowseLogRepository browseLogRepository,
                      OperationLogRepository operationLogRepository) {
        this.browseLogRepository = browseLogRepository;
        this.operationLogRepository = operationLogRepository;
    }

    // 记录浏览日志
    public void logBrowse(User user, Product product, String category,
                          Integer stayDuration, HttpServletRequest request) {
        BrowseLog log = new BrowseLog();
        log.setUser(user);
        log.setProduct(product);
        log.setCategory(category);
        log.setStayDuration(stayDuration);
        log.setIpAddress(getClientIp(request));
        browseLogRepository.save(log);
    }

    // 记录操作日志（销售人员/管理员）
    public void logOperation(String username, String role, String operation,
                              HttpServletRequest request) {
        OperationLog log = new OperationLog();
        log.setUsername(username);
        log.setRole(role);
        log.setOperation(operation);
        log.setIpAddress(getClientIp(request));
        operationLogRepository.save(log);
    }

    // 获取用户浏览历史
    public List<BrowseLog> getUserBrowseLogs(User user) {
        return browseLogRepository.findByUserOrderByBrowseTimeDesc(user);
    }

    // 获取用户偏好类别
    public List<Object[]> getUserCategoryPreferences(User user) {
        return browseLogRepository.findUserCategoryPreferences(user);
    }

    // 获取热门浏览类别
    public List<Object[]> getHotCategories(String period) {
        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        switch (period) {
            case "day":
                start = LocalDate.now().atStartOfDay();
                break;
            case "week":
                start = LocalDate.now().minusDays(7).atStartOfDay();
                break;
            case "month":
                start = LocalDate.now().minusDays(30).atStartOfDay();
                break;
            default:
                start = LocalDate.now().minusDays(7).atStartOfDay();
                break;
        }
        return browseLogRepository.findCategoryBrowseCount(start, end);
    }

    // 获取活跃用户
    public List<Object[]> getMostActiveUsers(String period) {
        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        switch (period) {
            case "day":
                start = LocalDate.now().atStartOfDay();
                break;
            case "week":
                start = LocalDate.now().minusDays(7).atStartOfDay();
                break;
            case "month":
                start = LocalDate.now().minusDays(30).atStartOfDay();
                break;
            default:
                start = LocalDate.now().minusDays(7).atStartOfDay();
                break;
        }
        return browseLogRepository.findMostActiveUsers(start, end);
    }

    // 获取所有操作日志
    public List<OperationLog> getAllOperationLogs() {
        return operationLogRepository.findAllByOrderByOperationTimeDesc();
    }

    // 获取用户操作日志
    public List<OperationLog> getUserOperationLogs(String username) {
        return operationLogRepository.findByUsernameOrderByOperationTimeDesc(username);
    }

    // 获取今日浏览量
    public long getTodayBrowseCount() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return browseLogRepository.countByBrowseTimeBetween(start, end);
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
