package com.tribiz.controller;

import com.tribiz.dto.request.ProductRequest;
import com.tribiz.dto.response.MessageResponse;
import com.tribiz.entity.Product;
import com.tribiz.entity.Shop;
import com.tribiz.repository.ProductRepository;
import com.tribiz.repository.ShopRepository;
import com.tribiz.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ShopRepository shopRepository;

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductRequest productRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        
        List<Shop> userShops = shopRepository.findByOwnerId(userDetails.getId());
        if (userShops.isEmpty()) {
            throw new RuntimeException("Error: You don't have any shops!");
        }

        // Use the first shop for now - in a real app, you might want to let the user choose which shop
        Shop shop = userShops.get(0);

        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .quantity(productRequest.getQuantity())
                .image(productRequest.getImage())
                .status(productRequest.getStatus())
                .barcode(productRequest.getBarcode())
                .category(productRequest.getCategory())
                .discount(productRequest.getDiscount())
                .buyOneGetOne(productRequest.getBuyOneGetOne())
                .shop(shop)
                .build();

        productRepository.save(product);
        return ResponseEntity.ok(new MessageResponse("Product added successfully!"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> updateProduct(@PathVariable("id") Long id, @Valid @RequestBody ProductRequest productRequest) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Product not found!"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (!product.getShop().getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You don't own this product!"));
        }

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setQuantity(productRequest.getQuantity());
        product.setImage(productRequest.getImage());
        product.setStatus(productRequest.getStatus());
        product.setBarcode(productRequest.getBarcode());
        product.setCategory(productRequest.getCategory());
        product.setDiscount(productRequest.getDiscount());
        product.setBuyOneGetOne(productRequest.getBuyOneGetOne());

        productRepository.save(product);
        return ResponseEntity.ok(new MessageResponse("Product updated successfully!"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Product not found!"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (!product.getShop().getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You don't own this product!"));
        }

        productRepository.delete(product);
        return ResponseEntity.ok(new MessageResponse("Product deleted successfully!"));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<Product>> getProductsByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(productRepository.findByShopId(shopId));
    }

    /**
     * Get current seller's products
     */
    @GetMapping("/my-products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<Product>> getMyProducts() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        
        List<Shop> userShops = shopRepository.findByOwnerId(userDetails.getId());
        if (userShops.isEmpty()) {
            return ResponseEntity.ok(List.of()); // Return empty list if no shops
        }

        // Aggregate products from all user's shops
        List<Product> allProducts = new java.util.ArrayList<>();
        for (Shop shop : userShops) {
            allProducts.addAll(productRepository.findByShopId(shop.getId()));
        }
        
        return ResponseEntity.ok(allProducts);
    }

    /**
     * Get low stock products for current seller
     */
    @GetMapping("/my-products/low-stock")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<Product>> getMyLowStockProducts() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        
        List<Shop> userShops = shopRepository.findByOwnerId(userDetails.getId());
        if (userShops.isEmpty()) {
            return ResponseEntity.ok(List.of()); // Return empty list if no shops
        }

        // Aggregate all products from user's shops
        List<Product> allProducts = new java.util.ArrayList<>();
        for (Shop shop : userShops) {
            allProducts.addAll(productRepository.findByShopId(shop.getId()));
        }
        
        List<Product> lowStockProducts = allProducts.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() <= p.getStockThreshold())
                .toList();

        return ResponseEntity.ok(lowStockProducts);
    }

    /**
     * Update product stock quantity
     */
    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> updateProductStock(@PathVariable Long id, @RequestParam Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Product not found!"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (!product.getShop().getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You don't own this product!"));
        }

        if (quantity < 0) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Quantity cannot be negative!"));
        }

        product.setQuantity(quantity);
        productRepository.save(product);
        return ResponseEntity.ok(new MessageResponse("Product stock updated to " + quantity));
    }

    /**
     * Update product stock threshold
     */
    @PutMapping("/{id}/stock-threshold")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> updateStockThreshold(@PathVariable Long id, @RequestParam Integer threshold) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Product not found!"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (!product.getShop().getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You don't own this product!"));
        }

        if (threshold < 0) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Threshold cannot be negative!"));
        }

        product.setStockThreshold(threshold);
        productRepository.save(product);
        return ResponseEntity.ok(new MessageResponse("Stock threshold updated to " + threshold));
    }

    /**
     * Get product stock status summary
     */
    @GetMapping("/my-products/stock-summary")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> getStockSummary() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        
        List<Shop> userShops = shopRepository.findByOwnerId(userDetails.getId());
        if (userShops.isEmpty()) {
            return ResponseEntity.ok(new StockSummaryResponse(0, 0, 0, 0)); // Return zero summary if no shops
        }

        // Aggregate all products from user's shops
        List<Product> allProducts = new java.util.ArrayList<>();
        for (Shop shop : userShops) {
            allProducts.addAll(productRepository.findByShopId(shop.getId()));
        }

        long totalProducts = allProducts.size();
        long criticalStock = allProducts.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() == 0)
                .count();
        long lowStock = allProducts.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() <= p.getStockThreshold())
                .count();
        long healthyStock = allProducts.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > p.getStockThreshold())
                .count();

        return ResponseEntity.ok(new StockSummaryResponse(totalProducts, criticalStock, lowStock, healthyStock));
    }

    public record StockSummaryResponse(long totalProducts, long criticalStock, long lowStock, long healthyStock) {}

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }
}
