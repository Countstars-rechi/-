package com.example.ecommerce.service;

import com.example.ecommerce.entity.*;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final UserService userService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartService cartService,
                        UserService userService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
        this.userService = userService;
    }

    // 从购物车创建订单（结算）
    @Transactional
    public Order createOrderFromCart(String username, String shippingAddress,
                                      String receiverName, String receiverPhone) {
        User user = userService.getCurrentUser(username);
        List<CartItem> cartItems = cartService.getCartItems(username);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUser(user);
        order.setStatus("PENDING");
        order.setShippingAddress(shippingAddress);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            total = total.add(cartItem.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItems.add(orderItem);
        }

        order.setTotalAmount(total);
        order.setOrderItems(orderItems);
        order = orderRepository.save(order);

        // 清空购物车
        cartService.clearCart(username);

        return order;
    }

    // 直接购买（单个商品）
    @Transactional
    public Order createOrderDirect(String username, Long productId, Integer quantity,
                                    String shippingAddress, String receiverName, String receiverPhone) {
        User user = userService.getCurrentUser(username);
        Product product = cartService.getProductService().getProductById(productId);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUser(user);
        order.setStatus("PENDING");
        order.setShippingAddress(shippingAddress);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setPrice(product.getPrice());

        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        order.setOrderItems(Collections.singletonList(orderItem));

        return orderRepository.save(order);
    }

    // 支付订单（模拟）
    @Transactional
    public Order payOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // 获取用户订单
    public List<Order> getUserOrders(String username) {
        User user = userService.getCurrentUser(username);
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // 获取所有订单（管理员/销售人员）
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    // 获取订单详情
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    // 获取销售统计
    public Double getTotalSales() {
        Double sales = orderRepository.getTotalSales();
        return sales != null ? sales : 0.0;
    }

    // 获取今日销售
    public Double getTodaySales() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        Double sales = orderRepository.getSalesByDateRange(start, end);
        return sales != null ? sales : 0.0;
    }

    // 获取本周销售
    public Double getWeekSales() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);
        Double sales = orderRepository.getSalesByDateRange(start, end);
        return sales != null ? sales : 0.0;
    }

    // 获取本月销售
    public Double getMonthSales() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime start = monthStart.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);
        Double sales = orderRepository.getSalesByDateRange(start, end);
        return sales != null ? sales : 0.0;
    }

    // 获取商品销售排行榜
    public List<Object[]> getTopSellingProducts(String period) {
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
                return orderItemRepository.findAllTimeTopSellingProducts();
        }
        return orderItemRepository.findTopSellingProducts(start, end);
    }

    // 获取各类别销售数据
    public List<Object[]> getSalesByCategory(String period) {
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
                start = LocalDate.now().minusDays(30).atStartOfDay();
                break;
        }
        return orderItemRepository.findSalesByCategory(start, end);
    }

    // 获取订单数量统计
    public Map<String, Long> getOrderStatusCount() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("PENDING", orderRepository.countByStatus("PENDING"));
        counts.put("PAID", orderRepository.countByStatus("PAID"));
        counts.put("COMPLETED", orderRepository.countByStatus("COMPLETED"));
        counts.put("CANCELLED", orderRepository.countByStatus("CANCELLED"));
        return counts;
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }
}
