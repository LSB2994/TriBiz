package com.tribiz.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Promotion entity for managing shop promotions and discounts.
 * Supports various promotion types: PERCENTAGE, FIXED_AMOUNT, BOGO (Buy One Get One)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PromotionType type;

    /**
     * For PERCENTAGE: discountValue is percentage (e.g., 20 for 20%)
     * For FIXED_AMOUNT: discountValue is amount to deduct (e.g., 10.00)
     * For BOGO: discountValue is typically null or 0
     */
    @Column(name = "promotion_value")
    private BigDecimal discountValue;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PromotionStatus status = PromotionStatus.ACTIVE;

    /**
     * Minimum purchase amount required to apply promotion
     */
    private BigDecimal minimumPurchase;

    /**
     * Maximum discount amount (for percentage promotions)
     */
    private BigDecimal maxDiscountAmount;

    /**
     * Usage limit (null for unlimited)
     */
    private Integer usageLimit;

    /**
     * Current usage count
     */
    @Builder.Default
    private Integer usageCount = 0;

    /**
     * Per-customer usage limit
     */
    private Integer perCustomerLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceItem service;

    /**
     * Code for customers to redeem (optional)
     */
    private String promoCode;

    /**
     * Whether promotion applies to all products/services in shop
     */
    @Builder.Default
    private Boolean appliesToAll = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PromotionType {
        PERCENTAGE,      // Percentage discount (e.g., 20% off)
        FIXED_AMOUNT,    // Fixed amount off (e.g., $10 off)
        BOGO,           // Buy One Get One Free
        BUY_X_GET_Y     // Buy X Get Y (customizable)
    }

    public enum PromotionStatus {
        ACTIVE,         // Currently running
        SCHEDULED,      // Will start in future
        EXPIRED,        // End date passed
        PAUSED,         // Temporarily stopped
        DISABLED        // Manually disabled
    }
}
