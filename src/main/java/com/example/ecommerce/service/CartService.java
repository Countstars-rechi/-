package com.example.ecommerce.service;

import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.CartItemRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserService userService;

    public CartService(CartItemRepository cartItemRepository,
                       ProductService productService,
                       UserService userService) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.userService = userService;
    }

    // 供OrderService使用
    public ProductService getProductService() {
        return productService;
    }

    public void addToCart(String username, Long productId, Integer quantity) {
        User user = userService.getCurrentUser(username);
        Product product = productService.getProductById(productId);

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product);
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
        }
        cartItemRepository.save(cartItem);
    }

    public List<CartItem> getCartItems(String username) {
        User user = userService.getCurrentUser(username);
        return cartItemRepository.findByUser(user);
    }

    public void removeCartItem(String username, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));
        if (!item.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("无权操作此购物车项");
        }
        cartItemRepository.delete(item);
    }

    public void clearCart(String username) {
        User user = userService.getCurrentUser(username);
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        cartItemRepository.deleteAll(cartItems);
    }
}
