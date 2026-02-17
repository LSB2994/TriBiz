package com.tribiz.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating and updating promotions
 */
@Data
public class PromotionRequest {

    @NotBlank(message = "Promotion name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Promotion type is required")
    private String type; // PERCENTAGE, FIXED_AMOUNT, BOGO, BUY_X_GET_Y

    @DecimalMin(value = "0.00", message = "Value must be positive")
    @DecimalMax(value = "100.00", message = "Percentage cannot exceed 100")
    private BigDecimal value;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @DecimalMin(value = "0.00", message = "Minimum purchase must be positive")
    private BigDecimal minimumPurchase;

    @DecimalMin(value = "0.00", message = "Maximum discount must be positive")
    private BigDecimal maxDiscountAmount;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @Min(value = 1, message = "Per-customer limit must be at least 1")
    private Integer perCustomerLimit;

    private Long productId;

    private Long serviceId;

    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Promo code can only contain uppercase letters, numbers, hyphens, and underscores")
    @Size(min = 3, max = 20, message = "Promo code must be between 3 and 20 characters")
    private String promoCode;

    private Boolean appliesToAll;
}
