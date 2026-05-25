package com.example.ecommerce.service;

import com.example.ecommerce.entity.*;
import com.example.ecommerce.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BrowseLogRepository browseLogRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public AnalysisService(OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           BrowseLogRepository browseLogRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.browseLogRepository = browseLogRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // ========== 用户画像 ==========

    /**
     * 用户画像分析
     * 返回：基本信息、购买力等级、偏好类别、活跃度等
     */
    public Map<String, Object> getUserProfile(String username) {
        Map<String, Object> profile = new HashMap<>();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return profile;

        // 基本信息
        profile.put("username", user.getUsername());
        profile.put("fullName", user.getFullName() != null ? user.getFullName() : "");
        profile.put("email", user.getEmail() != null ? user.getEmail() : "");
        profile.put("phone", user.getPhone() != null ? user.getPhone() : "");
        profile.put("region", user.getRegion() != null ? user.getRegion() : "未知");
        profile.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");

        // 购买力分析（基于历史订单总金额）
        List<Order> orders = orderRepository.findByUser(user);
        double totalSpent = orders.stream()
                .filter(o -> "PAID".equals(o.getStatus()) || "COMPLETED".equals(o.getStatus()))
                .mapToDouble(o -> o.getTotalAmount().doubleValue())
                .sum();
        int totalOrders = (int) orders.stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .count();
        double avgOrderAmount = totalOrders > 0 ? totalSpent / totalOrders : 0;

        String purchasingPower;
        if (totalSpent > 10000) purchasingPower = "高";
        else if (totalSpent > 5000) purchasingPower = "中";
        else purchasingPower = "低";

        profile.put("purchasingPower", purchasingPower);
        profile.put("totalSpent", totalSpent);
        profile.put("totalOrders", totalOrders);
        profile.put("avgOrderAmount", avgOrderAmount);

        // 活跃度（基于浏览记录数量）
        List<BrowseLog> browseLogs = browseLogRepository.findByUser(user);
        long browseCount = browseLogs.size();
        String activityLevel;
        if (browseCount > 50) activityLevel = "高";
        else if (browseCount > 10) activityLevel = "中";
        else activityLevel = "低";
        profile.put("activityLevel", activityLevel);

        // 偏好类别
        List<String> preferredCategories = browseLogRepository.findUserCategoryPreferences(user)
                .stream()
                .map(pref -> (String) pref[0])
                .collect(Collectors.toList());
        profile.put("preferredCategories", preferredCategories);

        // 最近浏览（取最近5条）
        List<String> recentBrowses = browseLogRepository.findByUserOrderByBrowseTimeDesc(user)
                .stream()
                .limit(5)
                .map(b -> {
                    if (b.getProduct() != null) {
                        return b.getProduct().getName();
                    }
                    return "浏览了 " + (b.getCategory() != null ? b.getCategory() : "首页");
                })
                .collect(Collectors.toList());
        profile.put("recentBrowses", recentBrowses);

        return profile;
    }

    // ========== 销售趋势 ==========

    /**
     * 销售趋势数据（用于图表）
     * 返回：按日期的销售额列表
     */
    public Map<String, Object> getSalesTrend(String period) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Double> sales = new ArrayList<>();

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;

        switch (period) {
            case "week":
                start = LocalDate.now().minusDays(6).atStartOfDay();
                for (int i = 0; i < 7; i++) {
                    LocalDate day = LocalDate.now().minusDays(6 - i);
                    dates.add(day.toString());
                    LocalDateTime dayStart = day.atStartOfDay();
                    LocalDateTime dayEnd = day.atTime(LocalTime.MAX);
                    Double daySales = orderRepository.getSalesByDateRange(dayStart, dayEnd);
                    sales.add(daySales != null ? daySales : 0.0);
                }
                break;
            case "month":
                start = LocalDate.now().minusDays(29).atStartOfDay();
                for (int i = 0; i < 30; i++) {
                    LocalDate day = LocalDate.now().minusDays(29 - i);
                    dates.add(day.toString());
                    LocalDateTime dayStart = day.atStartOfDay();
                    LocalDateTime dayEnd = day.atTime(LocalTime.MAX);
                    Double daySales = orderRepository.getSalesByDateRange(dayStart, dayEnd);
                    sales.add(daySales != null ? daySales : 0.0);
                }
                break;
            default:
                start = LocalDate.now().minusDays(6).atStartOfDay();
                for (int i = 0; i < 7; i++) {
                    LocalDate day = LocalDate.now().minusDays(6 - i);
                    dates.add(day.toString());
                    LocalDateTime dayStart = day.atStartOfDay();
                    LocalDateTime dayEnd = day.atTime(LocalTime.MAX);
                    Double daySales = orderRepository.getSalesByDateRange(dayStart, dayEnd);
                    sales.add(daySales != null ? daySales : 0.0);
                }
                break;
        }

        result.put("dates", dates);
        result.put("sales", sales);
        return result;
    }

    // ========== 销售排行榜 ==========

    /**
     * 商品销售排行榜
     */
    public List<Map<String, Object>> getProductRanking(String period) {
        List<Object[]> rankingData = orderItemRepository.findAllTimeTopSellingProducts();
        List<Map<String, Object>> ranking = new ArrayList<>();
        int rank = 1;
        for (Object[] data : rankingData) {
            Product product = (Product) data[0];
            Long totalQty = (Long) data[1];
            Map<String, Object> item = new HashMap<>();
            item.put("rank", rank++);
            item.put("productId", product.getId());
            item.put("productName", product.getName());
            item.put("category", product.getCategory());
            item.put("price", product.getPrice());
            item.put("totalSold", totalQty);
            ranking.add(item);
        }
        return ranking;
    }

    // ========== 销售异常检测 ==========

    /**
     * 简单销售异常检测
     * 如果某天销售额低于平均值的50%或高于平均值的200%，视为异常
     */
    public Map<String, Object> detectSalesAnomalies() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> anomalies = new ArrayList<>();

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = LocalDate.now().minusDays(7).atStartOfDay();

        // 计算7天平均销售额
        double totalSales = 0;
        int dayCount = 0;
        Map<LocalDate, Double> dailySales = new HashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate day = LocalDate.now().minusDays(6 - i);
            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd = day.atTime(LocalTime.MAX);
            Double daySales = orderRepository.getSalesByDateRange(dayStart, dayEnd);
            double sales = daySales != null ? daySales : 0.0;
            dailySales.put(day, sales);
            totalSales += sales;
            dayCount++;
        }

        double avgSales = dayCount > 0 ? totalSales / dayCount : 0;

        // 检测异常
        for (Map.Entry<LocalDate, Double> entry : dailySales.entrySet()) {
            Map<String, Object> anomaly = new HashMap<>();
            anomaly.put("date", entry.getKey().toString());
            anomaly.put("sales", entry.getValue());
            anomaly.put("average", avgSales);

            if (avgSales > 0) {
                if (entry.getValue() < avgSales * 0.5) {
                    anomaly.put("type", "偏低");
                    anomaly.put("level", "警告");
                    anomalies.add(anomaly);
                } else if (entry.getValue() > avgSales * 2) {
                    anomaly.put("type", "偏高");
                    anomaly.put("level", "注意");
                    anomalies.add(anomaly);
                }
            }
        }

        result.put("averageSales", avgSales);
        result.put("anomalies", anomalies);
        result.put("totalSales", totalSales);
        return result;
    }

    // ========== 推荐系统（简单版） ==========

    /**
     * "浏览过此商品的人也买了..." 推荐
     * 基于购买该商品的用户还买了什么
     */
    public List<Product> getRecommendations(Long productId, int limit) {
        // 获取购买过该商品的所有订单
        List<Order> allOrders = orderRepository.findAllByOrderByCreatedAtDesc();
        Set<Long> userIds = new HashSet<>();

        // 找出购买过该商品的用户
        for (Order order : allOrders) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProduct().getId().equals(productId)) {
                    userIds.add(order.getUser().getId());
                    break;
                }
            }
        }

        // 这些用户还买了什么
        Map<Long, Long> productCount = new HashMap<>();
        for (Order order : allOrders) {
            if (userIds.contains(order.getUser().getId())) {
                for (OrderItem item : order.getOrderItems()) {
                    if (!item.getProduct().getId().equals(productId)) {
                        productCount.merge(item.getProduct().getId(), item.getQuantity().longValue(), Long::sum);
                    }
                }
            }
        }

        return productCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> productRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ========== 协同过滤推荐（UserCF） ==========

    /**
     * 基于用户的协同过滤推荐（UserCF）
     * 1. 构建用户-商品购买矩阵
     * 2. 计算目标用户与其他用户的余弦相似度
     * 3. 基于相似用户的购买行为生成推荐
     * 冷启动：新用户或无购买记录用户回退到简单推荐
     */
    public List<Product> getCFRecommendations(Long userId, Long productId, int limit) {
        List<Order> allOrders = orderRepository.findAllByOrderByCreatedAtDesc();

        // 构建用户-商品矩阵
        Map<Long, Map<Long, Double>> userProductMatrix = new HashMap<>();
        for (Order order : allOrders) {
            if ("CANCELLED".equals(order.getStatus())) continue;
            Long uid = order.getUser().getId();
            userProductMatrix.putIfAbsent(uid, new HashMap<>());
            for (OrderItem item : order.getOrderItems()) {
                Long pid = item.getProduct().getId();
                userProductMatrix.get(uid).merge(pid, (double) item.getQuantity(), Double::sum);
            }
        }

        Map<Long, Double> targetVector = userProductMatrix.getOrDefault(userId, new HashMap<>());

        // 冷启动：用户无购买记录，回退到简单推荐
        if (targetVector.isEmpty() && productId != null) {
            return getRecommendations(productId, limit);
        }

        // 计算与其他用户的余弦相似度
        Map<Long, Double> similarities = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : userProductMatrix.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) continue;
            double sim = cosineSimilarity(targetVector, entry.getValue());
            if (sim > 0) {
                similarities.put(otherUserId, sim);
            }
        }

        // 按相似度排序，取前20个相似用户
        List<Long> similarUsers = similarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 统计推荐商品：相似用户买过但目标用户没买过的商品
        Map<Long, Double> scores = new HashMap<>();
        for (Long suid : similarUsers) {
            double sim = similarities.get(suid);
            Map<Long, Double> vector = userProductMatrix.get(suid);
            for (Map.Entry<Long, Double> e : vector.entrySet()) {
                Long pid = e.getKey();
                if (!targetVector.containsKey(pid)) {
                    scores.merge(pid, sim * e.getValue(), Double::sum);
                }
            }
        }

        List<Product> cfResults = scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(e -> productRepository.findById(e.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 协同过滤结果不足时用简单推荐补充
        if (cfResults.size() < limit && productId != null) {
            List<Product> fallback = getRecommendations(productId, limit - cfResults.size());
            for (Product p : fallback) {
                if (!cfResults.contains(p)) {
                    cfResults.add(p);
                }
            }
        }

        return cfResults;
    }

    /**
     * 余弦相似度
     */
    private double cosineSimilarity(Map<Long, Double> a, Map<Long, Double> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        double dotProduct = 0, normA = 0, normB = 0;
        Set<Long> allKeys = new HashSet<>(a.keySet());
        allKeys.addAll(b.keySet());
        for (Long key : allKeys) {
            double va = a.getOrDefault(key, 0.0);
            double vb = b.getOrDefault(key, 0.0);
            dotProduct += va * vb;
            normA += va * va;
            normB += vb * vb;
        }
        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0 ? 0 : dotProduct / denominator;
    }

    // ========== 仪表盘数据 ==========

    /**
     * 获取管理仪表盘汇总数据
     */
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 销售统计
        data.put("totalSales", orderRepository.getTotalSales() != null ? orderRepository.getTotalSales() : 0.0);
        data.put("todaySales", getSalesForPeriod("today"));
        data.put("weekSales", getSalesForPeriod("week"));
        data.put("monthSales", getSalesForPeriod("month"));

        // 订单统计
        data.put("pendingOrders", orderRepository.countByStatus("PENDING"));
        data.put("paidOrders", orderRepository.countByStatus("PAID"));
        data.put("completedOrders", orderRepository.countByStatus("COMPLETED"));

        // 用户统计
        data.put("customerCount", userRepository.countByRole("CUSTOMER"));
        data.put("salesCount", userRepository.countByRole("SALES"));

        // 商品统计
        data.put("productCount", productRepository.count());

        // 今日浏览量
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        data.put("todayBrowseCount", browseLogRepository.countByBrowseTimeBetween(todayStart, todayEnd));

        return data;
    }

    private double getSalesForPeriod(String period) {
        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        switch (period) {
            case "today":
                start = LocalDate.now().atStartOfDay();
                break;
            case "week":
                start = LocalDate.now().minusDays(7).atStartOfDay();
                break;
            case "month":
                start = LocalDate.now().minusDays(30).atStartOfDay();
                break;
            default:
                start = LocalDate.now().atStartOfDay();
                break;
        }
        Double sales = orderRepository.getSalesByDateRange(start, end);
        return sales != null ? sales : 0.0;
    }
}
