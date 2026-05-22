package com.example.ecommerce.controller;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;
    private final LogService logService;
    private final AnalysisService analysisService;

    public AdminController(UserService userService,
                           OrderService orderService,
                           LogService logService,
                           AnalysisService analysisService) {
        this.userService = userService;
        this.orderService = orderService;
        this.logService = logService;
        this.analysisService = analysisService;
    }

    // 管理仪表盘
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("dashboard", analysisService.getDashboardData());
        model.addAttribute("trend", analysisService.getSalesTrend("week"));
        model.addAttribute("anomalies", analysisService.detectSalesAnomalies());
        return "admin/dashboard";
    }

    // 销售人员管理
    @GetMapping("/sales-users")
    public String salesUsers(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("salesUsers", userService.getAllSales());
        return "admin/sales-users";
    }

    // 添加销售人员页面
    @GetMapping("/sales-users/add")
    public String addSalesPage(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        return "admin/sales-user-form";
    }

    // 添加销售人员
    @PostMapping("/sales-users/add")
    public String addSalesUser(@RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String fullName,
                                Authentication authentication,
                                HttpServletRequest request) {
        try {
            userService.createSalesUser(username, password, fullName);
            logService.logOperation(authentication.getName(), "ADMIN",
                    "添加销售人员: " + username, request);
            return "redirect:/admin/sales-users?added";
        } catch (Exception e) {
            return "redirect:/admin/sales-users/add?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    // 重置密码
    @PostMapping("/sales-users/reset-password")
    public String resetPassword(@RequestParam String username,
                                 @RequestParam String newPassword,
                                 Authentication authentication,
                                 HttpServletRequest request) {
        userService.resetPassword(username, newPassword);
        logService.logOperation(authentication.getName(), "ADMIN",
                "重置密码: " + username, request);
        return "redirect:/admin/sales-users?reset";
    }

    // 启用/禁用账号
    @GetMapping("/sales-users/toggle/{username}")
    public String toggleUser(@PathVariable String username,
                              Authentication authentication,
                              HttpServletRequest request) {
        User user = userService.getCurrentUser(username);
        userService.setUserEnabled(username, !user.isEnabled());
        logService.logOperation(authentication.getName(), "ADMIN",
                (user.isEnabled() ? "禁用" : "启用") + "账号: " + username, request);
        return "redirect:/admin/sales-users?toggled";
    }

    // 销售业绩查询
    @GetMapping("/sales-performance")
    public String salesPerformance(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("totalSales", orderService.getTotalSales());
        model.addAttribute("todaySales", orderService.getTodaySales());
        model.addAttribute("weekSales", orderService.getWeekSales());
        model.addAttribute("monthSales", orderService.getMonthSales());
        model.addAttribute("ranking", analysisService.getProductRanking("all"));
        model.addAttribute("trend", analysisService.getSalesTrend("month"));
        model.addAttribute("orderStatusCount", orderService.getOrderStatusCount());
        return "admin/sales-performance";
    }

    // 操作日志
    @GetMapping("/operation-logs")
    public String operationLogs(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("logs", logService.getAllOperationLogs());
        return "admin/operation-logs";
    }

    // 用户画像分析
    @GetMapping("/user-profile")
    public String userProfile(@RequestParam(defaultValue = "") String username,
                               Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        if (!username.isEmpty()) {
            model.addAttribute("profile", analysisService.getUserProfile(username));
            model.addAttribute("searchUsername", username);
        }
        return "admin/user-profile";
    }
}
