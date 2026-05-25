package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    public CartController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @PostMapping("/add")
    public String addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication) {
        String username = authentication.getName();
        cartService.addToCart(username, productId, quantity);
        return "redirect:/products";
    }

    @PostMapping("/buy-now")
    public String buyNow(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication) {
        String username = authentication.getName();
        cartService.addToCart(username, productId, quantity);
        return "redirect:/cart/checkout";
    }

    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("cartItems", cartService.getCartItems(username));
        model.addAttribute("username", username);
        return "cart";
    }

    @PostMapping("/remove/{id}")
    public String removeCartItem(@PathVariable Long id, Authentication authentication) {
        cartService.removeCartItem(authentication.getName(), id);
        return "redirect:/cart";
    }

    // 结算页面（填写收货信息）
    @GetMapping("/checkout")
    public String checkoutPage(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("cartItems", cartService.getCartItems(username));
        model.addAttribute("username", username);
        return "checkout";
    }

    // 提交订单
    @PostMapping("/checkout")
    public String submitOrder(
            @RequestParam String shippingAddress,
            @RequestParam String receiverName,
            @RequestParam String receiverPhone,
            Authentication authentication,
            Model model) {
        try {
            String username = authentication.getName();
            Order order = orderService.createOrderFromCart(username, shippingAddress,
                    receiverName, receiverPhone);
            return "redirect:/orders/" + order.getId() + "?success";
        } catch (Exception e) {
            return "redirect:/cart/checkout?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }
}
