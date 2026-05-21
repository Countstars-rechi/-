package com.example.ecommerce.config;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init-data:true}")
    private boolean initData;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.fullName:系统管理员}")
    private String adminFullName;

    @Value("${app.sales.username:sales01}")
    private String salesUsername;

    @Value("${app.sales.password:sales123}")
    private String salesPassword;

    @Value("${app.sales.fullName:销售员小王}")
    private String salesFullName;

    @Value("${app.test.username:testuser}")
    private String testUsername;

    @Value("${app.test.password:123456}")
    private String testPassword;

    @Value("${app.test.fullName:测试用户}")
    private String testFullName;

    public DataInitializer(UserRepository userRepository,
                           ProductRepository productRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!initData) {
            return;
        }

        // 初始化用户
        initUsers();

        // 初始化商品
        initProducts();
    }

    private void initUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        // 管理员
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setFullName(adminFullName);
        admin.setRole("ADMIN");
        admin.setEnabled(true);
        userRepository.save(admin);

        // 销售人员
        User sales = new User();
        sales.setUsername(salesUsername);
        sales.setPassword(passwordEncoder.encode(salesPassword));
        sales.setFullName(salesFullName);
        sales.setRole("SALES");
        sales.setEnabled(true);
        userRepository.save(sales);

        // 测试用户
        User testUser = new User();
        testUser.setUsername(testUsername);
        testUser.setPassword(passwordEncoder.encode(testPassword));
        testUser.setFullName(testFullName);
        testUser.setRole("USER");
        testUser.setEnabled(true);
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setRegion("北京");
        userRepository.save(testUser);
    }

    private void initProducts() {
        if (productRepository.count() > 0) {
            return;
        }

        // 手机类
        productRepository.save(createProduct("iPhone 15 Pro Max",
                "苹果最新旗舰手机，A17 Pro芯片，钛金属设计",
                new BigDecimal("9999"), 100, "手机",
                "https://via.placeholder.com/300x200?text=iPhone+15"));

        productRepository.save(createProduct("华为 Mate 60 Pro",
                "华为旗舰手机，麒麟芯片，卫星通信",
                new BigDecimal("8999"), 80, "手机",
                "https://via.placeholder.com/300x200?text=Huawei+Mate60"));

        productRepository.save(createProduct("小米 14 Ultra",
                "小米影像旗舰，徕卡光学镜头",
                new BigDecimal("5999"), 120, "手机",
                "https://via.placeholder.com/300x200?text=Xiaomi+14"));

        // 平板类
        productRepository.save(createProduct("iPad Pro M4",
                "苹果最新平板，M4芯片，超XDR显示屏",
                new BigDecimal("8499"), 50, "平板",
                "https://via.placeholder.com/300x200?text=iPad+Pro"));

        productRepository.save(createProduct("华为 MatePad Pro",
                "华为旗舰平板，鸿蒙系统，天生会画",
                new BigDecimal("4299"), 60, "平板",
                "https://via.placeholder.com/300x200?text=MatePad+Pro"));

        // 耳机类
        productRepository.save(createProduct("AirPods Pro 2",
                "苹果主动降噪耳机，自适应音频",
                new BigDecimal("1899"), 200, "耳机",
                "https://via.placeholder.com/300x200?text=AirPods+Pro"));

        productRepository.save(createProduct("Sony WH-1000XM5",
                "索尼旗舰降噪耳机，行业顶级降噪",
                new BigDecimal("2999"), 70, "耳机",
                "https://via.placeholder.com/300x200?text=Sony+WH1000XM5"));

        // 电脑类
        productRepository.save(createProduct("MacBook Pro 14",
                "苹果笔记本，M3 Pro芯片，Liquid Retina XDR",
                new BigDecimal("14999"), 30, "电脑",
                "https://via.placeholder.com/300x200?text=MacBook+Pro"));

        productRepository.save(createProduct("ThinkPad X1 Carbon",
                "联想商务旗舰，轻薄便携，超长续航",
                new BigDecimal("10999"), 40, "电脑",
                "https://via.placeholder.com/300x200?text=ThinkPad+X1"));

        // 配件类
        productRepository.save(createProduct("Apple Watch Ultra 2",
                "苹果顶级智能手表，钛金属表壳",
                new BigDecimal("5999"), 45, "配件",
                "https://via.placeholder.com/300x200?text=Watch+Ultra"));

        productRepository.save(createProduct("MagSafe 充电器",
                "苹果磁吸无线充电器，15W快充",
                new BigDecimal("329"), 300, "配件",
                "https://via.placeholder.com/300x200?text=MagSafe"));

        productRepository.save(createProduct("罗技 MX Master 3S",
                "罗技旗舰鼠标，静音按键，电磁滚轮",
                new BigDecimal("899"), 150, "配件",
                "https://via.placeholder.com/300x200?text=MX+Master"));
    }

    private Product createProduct(String name, String description, BigDecimal price,
                                   Integer stock, String category, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        product.setEnabled(true);
        return product;
    }
}
