package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.AnalysisService;
import com.example.ecommerce.service.LogService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;
    private final UserService userService;
    private final LogService logService;
    private final AnalysisService analysisService;

    public ProductController(ProductService productService,
                             UserService userService,
                             LogService logService,
                             AnalysisService analysisService) {
        this.productService = productService;
        this.userService = userService;
        this.logService = logService;
        this.analysisService = analysisService;
    }

    @GetMapping("/products")
    public String productList(Model model, Authentication authentication,
                              @RequestParam(name = "category", required = false) String category,
                              @RequestParam(name = "keyword", required = false) String keyword,
                              HttpServletRequest request) {
        List<Product> products;
        if (keyword != null && !keyword.isEmpty()) {
            products = productService.searchProducts(keyword);
            model.addAttribute("keyword", keyword);
        } else if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
            model.addAttribute("selectedCategory", category);
        } else {
            products = productService.getAllProducts();
        }

        // 已登录用户记录浏览日志
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.getCurrentUser(username);
            logService.logBrowse(user, null, category != null ? category : "all", 10, request);
            model.addAttribute("username", username);
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllCategories());

        return "products";
    }

    @GetMapping("/product/detail")
    public String productDetail(@RequestParam("id") Long id, Model model,
                                 Authentication authentication,
                                 HttpServletRequest request) {
        Product product = productService.getProductById(id);

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.getCurrentUser(username);
            logService.logBrowse(user, product, product.getCategory(), 30, request);
            model.addAttribute("username", username);

            // 获取推荐商品
            List<Product> recommendations = analysisService.getRecommendations(id, 4);
            model.addAttribute("recommendations", recommendations);
        }

        model.addAttribute("product", product);

        return "product-detail";
    }
}
