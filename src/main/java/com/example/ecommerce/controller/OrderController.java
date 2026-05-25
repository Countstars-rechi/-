package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.service.OrderService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 我的订单列表
    @GetMapping
    public String myOrders(Model model, Authentication authentication) {
        String username = authentication.getName();
        List<Order> orders = orderService.getUserOrders(username);
        model.addAttribute("orders", orders);
        model.addAttribute("username", username);
        return "orders";
    }

    // 订单详情
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model,
                               Authentication authentication) {
        Order order = orderService.getOrderById(id);
        if (!order.getUser().getUsername().equals(authentication.getName())) {
            throw new AccessDeniedException("无权查看此订单");
        }
        model.addAttribute("order", order);
        model.addAttribute("username", authentication.getName());
        return "order-detail";
    }

    // 支付订单（模拟）
    @PostMapping("/{id}/pay")
    public String payOrder(@PathVariable Long id, Authentication authentication) {
        Order order = orderService.getOrderById(id);
        if (!order.getUser().getUsername().equals(authentication.getName())) {
            throw new AccessDeniedException("无权操作此订单");
        }
        orderService.payOrder(id);
        return "redirect:/orders/" + id + "?paid";
    }

    // 取消订单
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, Authentication authentication) {
        Order order = orderService.getOrderById(id);
        if (!order.getUser().getUsername().equals(authentication.getName())) {
            throw new AccessDeniedException("无权操作此订单");
        }
        try {
            orderService.cancelOrder(id);
            return "redirect:/orders/" + id + "?cancelled";
        } catch (Exception e) {
            return "redirect:/orders/" + id + "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }
}
