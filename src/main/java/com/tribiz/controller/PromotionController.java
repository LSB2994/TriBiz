package com.tribiz.controller;

import com.tribiz.dto.request.PromotionRequest;
import com.tribiz.dto.response.MessageResponse;
import com.tribiz.dto.response.PromotionResponse;
import com.tribiz.entity.Product;
import com.tribiz.entity.Promotion;
import com.tribiz.entity.ServiceItem;
import com.tribiz.entity.Shop;
import com.tribiz.repository.ProductRepository;
import com.tribiz.repository.PromotionRepository;
import com.tribiz.repository.ServiceItemRepository;
import com.tribiz.repository.ShopRepository;
import com.tribiz.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing promotions.
 * Supports CRUD operations for SELLER and SERVICE_PROVIDER roles.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    /**
     * Create a new promotion
     */
    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<?> createPromotion(@Valid @RequestBody PromotionRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        List<Shop> userShops = shopRepository.findByOwnerId(userDetails.getId());
        if (userShops.isEmpty()) {
            throw new RuntimeException("Error: You don't have any shops!");
        }

        // Use the first shop for now - in a real app, you might want to let the user choose which shop
        Shop shop = userShops.get(0);

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: End date must be after start date!"));
        }

        if (request.getEndDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: End date cannot be in the past!"));
        }

        Promotion.PromotionStatus status = request.getStartDate().isAfter(LocalDateTime.now())
                ? Promotion.PromotionStatus.SCHEDULED
                : Promotion.PromotionStatus.ACTIVE;

        Promotion.PromotionType type;
        try {
            type = Promotion.PromotionType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid promotion type! Valid types: PERCENTAGE, FIXED_AMOUNT, BOGO, BUY_X_GET_Y"));
        }

        Product product = null;
        ServiceItem service = null;

        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Error: Product not found!"));
            if (!product.getShop().getId().equals(shop.getId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Product does not belong to your shop!"));
            }
        }

        if (request.getServiceId() != null) {
            service = serviceItemRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Error: Service not found!"));
            if (!service.getShop().getId().equals(shop.getId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Service does not belong to your shop!"));
            }
        }

        Promotion promotion = Promotion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(type)
                .discountValue(request.getValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(status)
                .minimumPurchase(request.getMinimumPurchase())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .perCustomerLimit(request.getPerCustomerLimit())
                .promoCode(request.getPromoCode() != null ? request.getPromoCode().toUpperCase() : null)
                .appliesToAll(request.getAppliesToAll() != null ? request.getAppliesToAll() : false)
                .shop(shop)
                .product(product)
                .service(service)
                .build();

        promotionRepository.save(promotion);
        return ResponseEntity.ok(new MessageResponse("Promotion created successfully!"));
    }

    /**
     * Update an existing promotion
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<?> updatePromotion(@PathVariable("id") Long id, @Valid @RequestBody PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Promotion not found!"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        if (!promotion.getShop().getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: You don't own this promotion!"));
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: End date must be after start date!"));
        }

        Promotion.PromotionType type;
        try {
            type = Promotion.PromotionType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid promotion type!"));
        }

        // Update status based on dates if currently ACTIVE or SCHEDULED
        Promotion.PromotionStatus newStatus = promotion.getStatus();
        if (promotion.getStatus() == Promotion.PromotionStatus.ACTIVE || 
            promotion.getStatus() == Promotion.PromotionStatus.SCHEDULED) {
            newStatus = request.getStartDate().isAfter(LocalDateTime.now())
                    ? Promotion.PromotionStatus.SCHEDULED
                    : Promotion.PromotionStatus.ACTIVE;
        }

        Product product = null;
        ServiceItem service = null;

        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Error: Product not found!"));
        }

        if (request.getServiceId() != null) {
            service = serviceItemRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Error: Service not found!"));
        }

        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setType(type);
        promotion.setDiscountValue(request.getValue());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setStatus(newStatus);
        promotion.setMinimumPurchase(request.getMinimumPurchase());
        promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
        promotion.setUsageLimit(request.getUsageLimit());
        promotion.setPerCustomerLimit(request.getPerCustomerLimit());
        promotion.setPromoCode(request.getPromoCode() != null ? request.getPromoCode().toUpperCase() : null);
        promotion.setAppliesToAll(request.getAppliesToAll() != null ? request.getAppliesToAll() : false);
        promotion.setProduct(product);
        promotion.setService(service);

        promotionRepository.save(promotion);
        return ResponseEntity.ok(new MessageResponse("Promotion updated successfully!"));
    }

    /**
     * Delete a promotion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<?> deletePromotion(@PathVariable Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Promotion not found!"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        if (!promotion.getShop().getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: You don't own this promotion!"));
        }

        promotionRepository.delete(promotion);
        return ResponseEntity.ok(new MessageResponse("Promotion deleted successfully!"));
    }

    /**
     * Get all promotions (public endpoint)
     */
    @GetMapping
    public ResponseEntity<List<PromotionResponse>> getAllPromotions() {
        List<Promotion> promotions = promotionRepository.findAll();
        return ResponseEntity.ok(promotions.stream()
                .map(this::mapToPromotionResponse)
                .collect(Collectors.toList()));
    }

    /**
     * Get promotion by ID (public endpoint)
     */
    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getPromotionById(@PathVariable Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Promotion not found!"));
        return ResponseEntity.ok(mapToPromotionResponse(promotion));
    }

    /**
     * Get current user's promotions
     */
    @GetMapping("/my-promotions")
    @PreAuthorize("hasRole('SELLER') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<List<PromotionResponse>> getMyPromotions() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        List<Shop> userShops = shopRepository.findByOwnerId(userDetails.getId());
        if (userShops.isEmpty()) {
            throw new RuntimeException("Error: You don't have any shops!");
        }

        List<Promotion> allPromotions = new ArrayList<>();
        for (Shop shop : userShops) {
            List<Promotion> shopPromotions = promotionRepository.findByShopId(shop.getId());
            allPromotions.addAll(shopPromotions);
        }

        return ResponseEntity.ok(allPromotions.stream()
                .map(this::mapToPromotionResponse)
                .collect(Collectors.toList()));
    }

    /**
     * Get active promotions for current user's shop
     */
    @GetMapping("/my-promotions/active")
    @PreAuthorize("hasRole('SELLER') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<List<PromotionResponse>> getMyActivePromotions() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        List<Shop> userShops = shopRepository.findByOwnerId(userDetails.getId());
        if (userShops.isEmpty()) {
            throw new RuntimeException("Error: You don't have any shops!");
        }

        List<Promotion> allActivePromotions = new ArrayList<>();
        for (Shop shop : userShops) {
            List<Promotion> activePromotions = promotionRepository.findActivePromotionsByShop(shop.getId(), LocalDateTime.now());
            allActivePromotions.addAll(activePromotions);
        }

        return ResponseEntity.ok(allActivePromotions.stream()
                .map(this::mapToPromotionResponse)
                .collect(Collectors.toList()));
    }

    /**
     * Get promotions by shop (public endpoint)
     */
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<PromotionResponse>> getPromotionsByShop(@PathVariable Long shopId) {
        List<Promotion> promotions = promotionRepository.findByShopId(shopId);
        return ResponseEntity.ok(promotions.stream()
                .map(this::mapToPromotionResponse)
                .collect(Collectors.toList()));
    }

    /**
     * Update promotion status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SELLER') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<?> updatePromotionStatus(@PathVariable Long id, @RequestParam String status) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Promotion not found!"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        if (!promotion.getShop().getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: You don't own this promotion!"));
        }

        Promotion.PromotionStatus newStatus;
        try {
            newStatus = Promotion.PromotionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid status! Valid statuses: ACTIVE, SCHEDULED, PAUSED, DISABLED"));
        }

        promotion.setStatus(newStatus);
        promotionRepository.save(promotion);
        return ResponseEntity.ok(new MessageResponse("Promotion status updated to " + status));
    }

    /**
     * Validate and apply a promo code
     */
    @PostMapping("/validate-code")
    public ResponseEntity<?> validatePromoCode(@RequestParam String code, @RequestParam(required = false) Long shopId) {
        List<Promotion> promotions = promotionRepository.findActiveByPromoCode(code.toUpperCase(), LocalDateTime.now());

        if (promotions.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid or expired promo code!"));
        }

        Promotion promotion = promotions.get(0);

        if (shopId != null && !promotion.getShop().getId().equals(shopId)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Promo code not valid for this shop!"));
        }

        if (promotion.getUsageLimit() != null && promotion.getUsageCount() >= promotion.getUsageLimit()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Promo code usage limit reached!"));
        }

        return ResponseEntity.ok(mapToPromotionResponse(promotion));
    }

    private PromotionResponse mapToPromotionResponse(Promotion promotion) {
        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .type(promotion.getType().name())
                .discountValue(promotion.getDiscountValue())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .status(promotion.getStatus().name())
                .minimumPurchase(promotion.getMinimumPurchase())
                .maxDiscountAmount(promotion.getMaxDiscountAmount())
                .usageLimit(promotion.getUsageLimit())
                .usageCount(promotion.getUsageCount())
                .perCustomerLimit(promotion.getPerCustomerLimit())
                .shopId(promotion.getShop() != null ? promotion.getShop().getId() : null)
                .shopName(promotion.getShop() != null ? promotion.getShop().getName() : null)
                .productId(promotion.getProduct() != null ? promotion.getProduct().getId() : null)
                .productName(promotion.getProduct() != null ? promotion.getProduct().getName() : null)
                .serviceId(promotion.getService() != null ? promotion.getService().getId() : null)
                .serviceName(promotion.getService() != null ? promotion.getService().getName() : null)
                .promoCode(promotion.getPromoCode())
                .appliesToAll(promotion.getAppliesToAll())
                .createdAt(promotion.getCreatedAt())
                .updatedAt(promotion.getUpdatedAt())
                .build();
    }
}
