package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.AnalysisService;
import com.example.ecommerce.service.LogService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              HttpServletRequest request) {
        int pageSize = 9;
        Page<Product> productPage;

        if (keyword != null && !keyword.isEmpty()) {
            productPage = productService.searchProductsPaged(keyword, page, pageSize);
            model.addAttribute("keyword", keyword);
        } else if (category != null && !category.isEmpty()) {
            productPage = productService.getProductsByCategoryPaged(category, page, pageSize);
            model.addAttribute("selectedCategory", category);
        } else {
            productPage = productService.getProductsPaged(page, pageSize);
        }

        // 已登录用户记录浏览日志
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            User user = userService.getCurrentUser(username);
            logService.logBrowse(user, null, category != null ? category : "all", 10, request);
            model.addAttribute("username", username);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("categories", productService.getAllCategories());

        return "products";
    }

    @GetMapping("/product/detail")
    public String productDetail(@RequestParam("id") Long id, Model model,
                                 Authentication authentication,
                                 HttpServletRequest request) {
        Product product = productService.getProductById(id);

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            User user = userService.getCurrentUser(username);
            logService.logBrowse(user, product, product.getCategory(), 30, request);
            model.addAttribute("username", username);

            // 协同过滤推荐（登录用户优先用UserCF，冷启动回退到物品推荐）
            List<Product> recommendations = analysisService.getCFRecommendations(
                    user.getId(), id, 4);
            model.addAttribute("recommendations", recommendations);
        }

        model.addAttribute("product", product);

        return "product-detail";
    }
}
