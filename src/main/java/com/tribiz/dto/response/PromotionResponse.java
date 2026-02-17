package com.tribiz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for promotion responses
 */
@Data
@Builder
public class PromotionResponse {

    private Long id;
    private String name;
    private String description;
    private String type;
    private BigDecimal discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private BigDecimal minimumPurchase;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer perCustomerLimit;
    private Long shopId;
    private String shopName;
    private Long productId;
    private String productName;
    private Long serviceId;
    private String serviceName;
    private String promoCode;
    private Boolean appliesToAll;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
