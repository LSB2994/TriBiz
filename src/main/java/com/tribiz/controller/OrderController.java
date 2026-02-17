package com.tribiz.controller;

import com.tribiz.dto.request.OrderItemRequest;
import com.tribiz.dto.request.OrderRequest;
import com.tribiz.dto.response.MessageResponse;
import com.tribiz.entity.*;
import com.tribiz.repository.OrderRepository;
import com.tribiz.repository.ProductRepository;
import com.tribiz.repository.ShopRepository;
import com.tribiz.repository.UserRepository;
import com.tribiz.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User customer = userRepository.findById(userDetails.getId()).orElseThrow();

        Shop shop = shopRepository.findById(orderRequest.getShopId())
                .orElseThrow(() -> new RuntimeException("Error: Shop not found!"));

        Order order = Order.builder()
                .customer(customer)
                .shop(shop)
                .orderDate(LocalDateTime.now())
                .status("PENDING")
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Error: Product not found!"));

            if (product.getQuantity() < itemRequest.getQuantity()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Not enough stock for product " + product.getName()));
            }

            // Deduct stock
            product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build();

            order.getOrderItems().add(orderItem);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        return ResponseEntity.ok(new MessageResponse("Order placed successfully!"));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Order>> getMyOrders() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return ResponseEntity.ok(orderRepository.findByCustomerId(userDetails.getId()));
    }

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getShopOrders(@PathVariable Long shopId) {
        // Verification logic can be added here
        return ResponseEntity.ok(orderRepository.findByShopId(shopId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Order not found!"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (!order.getShop().getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You don't own this shop!"));
        }

        order.setStatus(status);
        orderRepository.save(order);
        return ResponseEntity.ok(new MessageResponse("Order status updated to " + status));
    }
}
