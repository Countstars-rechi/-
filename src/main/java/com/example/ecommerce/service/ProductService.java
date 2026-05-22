package com.example.ecommerce.service;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findByEnabledTrue();
    }

    public Page<Product> getProductsPaged(int page, int size) {
        return productRepository.findByEnabledTrue(PageRequest.of(page, size));
    }

    public Page<Product> getProductsByCategoryPaged(String category, int page, int size) {
        return productRepository.findByCategory(category, PageRequest.of(page, size));
    }

    public Page<Product> searchProductsPaged(String keyword, int page, int size) {
        return productRepository.findByNameContaining(keyword, PageRequest.of(page, size));
    }

    public List<Product> getAllProductsForAdmin() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在：" + id));
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // 销售人员：添加商品
    public Product addProduct(String name, String description, BigDecimal price,
                               Integer stock, String category, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        return productRepository.save(product);
    }

    // 销售人员：更新商品
    public Product updateProduct(Long id, String name, String description, BigDecimal price,
                                  Integer stock, String category, String imageUrl, boolean enabled) {
        Product product = getProductById(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        product.setEnabled(enabled);
        return productRepository.save(product);
    }

    // 销售人员：删除商品（软删除）
    public void disableProduct(Long id) {
        Product product = getProductById(id);
        product.setEnabled(false);
        productRepository.save(product);
    }

    // 按类别查询
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    // 搜索商品
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }

    // 获取所有类别
    public List<String> getAllCategories() {
        return productRepository.findDistinctCategoryBy();
    }
}
