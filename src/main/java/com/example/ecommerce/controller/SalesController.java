package com.example.ecommerce.controller;

import com.example.ecommerce.entity.*;
import com.example.ecommerce.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/sales")
public class SalesController {

    private final ProductService productService;
    private final OrderService orderService;
    private final LogService logService;
    private final UserService userService;
    private final AnalysisService analysisService;

    public SalesController(ProductService productService,
                           OrderService orderService,
                           LogService logService,
                           UserService userService,
                           AnalysisService analysisService) {
        this.productService = productService;
        this.orderService = orderService;
        this.logService = logService;
        this.userService = userService;
        this.analysisService = analysisService;
    }

    // 销售仪表盘
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("username", username);
        model.addAttribute("dashboard", analysisService.getDashboardData());
        model.addAttribute("ranking", analysisService.getProductRanking("all"));
        model.addAttribute("anomalies", analysisService.detectSalesAnomalies());
        return "sales/dashboard";
    }

    // 商品管理列表
    @GetMapping("/products")
    public String productList(Model model, Authentication authentication) {
        model.addAttribute("products", productService.getAllProductsForAdmin());
        model.addAttribute("username", authentication.getName());
        return "sales/products";
    }

    // 添加商品页面
    @GetMapping("/products/add")
    public String addProductPage(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        return "sales/product-form";
    }

    // 添加商品
    @PostMapping("/products/add")
    public String addProduct(@RequestParam String name,
                              @RequestParam String description,
                              @RequestParam BigDecimal price,
                              @RequestParam(defaultValue = "0") Integer stock,
                              @RequestParam String category,
                              @RequestParam(defaultValue = "") String imageUrl,
                              Authentication authentication,
                              HttpServletRequest request) {
        productService.addProduct(name, description, price, stock, category, imageUrl);
        logService.logOperation(authentication.getName(), "SALES",
                "添加商品: " + name, request);
        return "redirect:/sales/products?added";
    }

    // 编辑商品页面
    @GetMapping("/products/edit/{id}")
    public String editProductPage(@PathVariable Long id, Model model,
                                   Authentication authentication) {
        model.addAttribute("product", productService.getProductById(id));
        model.addAttribute("username", authentication.getName());
        return "sales/product-form";
    }

    // 更新商品
    @PostMapping("/products/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam String description,
                                 @RequestParam BigDecimal price,
                                 @RequestParam Integer stock,
                                 @RequestParam String category,
                                 @RequestParam(defaultValue = "") String imageUrl,
                                 @RequestParam(defaultValue = "true") boolean enabled,
                                 Authentication authentication,
                                 HttpServletRequest request) {
        productService.updateProduct(id, name, description, price, stock, category, imageUrl, enabled);
        logService.logOperation(authentication.getName(), "SALES",
                "更新商品: " + name, request);
        return "redirect:/sales/products?updated";
    }

    // 下架商品
    @GetMapping("/products/disable/{id}")
    public String disableProduct(@PathVariable Long id,
                                  Authentication authentication,
                                  HttpServletRequest request) {
        Product product = productService.getProductById(id);
        productService.disableProduct(id);
        logService.logOperation(authentication.getName(), "SALES",
                "下架商品: " + product.getName(), request);
        return "redirect:/sales/products?disabled";
    }

    // 订单管理
    @GetMapping("/orders")
    public String orderList(Model model, Authentication authentication) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("username", authentication.getName());
        return "sales/orders";
    }

    // 发货
    @PostMapping("/orders/{id}/ship")
    public String shipOrder(@PathVariable Long id, Authentication authentication,
                             HttpServletRequest request) {
        try {
            orderService.shipOrder(id);
            Order order = orderService.getOrderById(id);
            logService.logOperation(authentication.getName(), "SALES",
                    "发货订单: " + order.getOrderNo(), request);
            return "redirect:/sales/orders?shipped";
        } catch (Exception e) {
            return "redirect:/sales/orders?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    // 完成订单
    @PostMapping("/orders/{id}/complete")
    public String completeOrder(@PathVariable Long id, Authentication authentication,
                                 HttpServletRequest request) {
        try {
            orderService.completeOrder(id);
            Order order = orderService.getOrderById(id);
            logService.logOperation(authentication.getName(), "SALES",
                    "完成订单: " + order.getOrderNo(), request);
            return "redirect:/sales/orders?completed";
        } catch (Exception e) {
            return "redirect:/sales/orders?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    // 分类管理
    @GetMapping("/categories")
    public String categories(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("categories", productService.getAllCategories());
        return "sales/categories";
    }

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam String categoryName,
                               Authentication authentication,
                               HttpServletRequest request) {
        logService.logOperation(authentication.getName(), "SALES",
                "添加分类: " + categoryName, request);
        return "redirect:/sales/categories?added&name=" + URLEncoder.encode(categoryName, StandardCharsets.UTF_8);
    }

    @GetMapping("/categories/delete")
    public String deleteCategory(@RequestParam String category,
                                  Authentication authentication,
                                  HttpServletRequest request) {
        List<Product> products = productService.getProductsByCategory(category);
        if (!products.isEmpty()) {
            return "redirect:/sales/categories?error=" + URLEncoder.encode(
                    "该分类下还有 " + products.size() + " 个商品，无法删除", StandardCharsets.UTF_8);
        }
        logService.logOperation(authentication.getName(), "SALES",
                "删除分类: " + category, request);
        return "redirect:/sales/categories?deleted";
    }

    // 销售统计
    @GetMapping("/statistics")
    public String statistics(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("username", username);
        model.addAttribute("totalSales", orderService.getTotalSales());
        model.addAttribute("todaySales", orderService.getTodaySales());
        model.addAttribute("weekSales", orderService.getWeekSales());
        model.addAttribute("monthSales", orderService.getMonthSales());
        model.addAttribute("trend", analysisService.getSalesTrend("week"));
        model.addAttribute("ranking", analysisService.getProductRanking("all"));
        model.addAttribute("categorySales", orderService.getSalesByCategory("month"));
        return "sales/statistics";
    }

    // 浏览日志
    @GetMapping("/browse-logs")
    public String browseLogs(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("hotCategories", logService.getHotCategories("week"));
        model.addAttribute("activeUsers", logService.getMostActiveUsers("week"));
        return "sales/browse-logs";
    }
}
