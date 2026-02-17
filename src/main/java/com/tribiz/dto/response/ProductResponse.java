package com.tribiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private String image;
    private String status;
    private String category;
    private BigDecimal discount;
    private Boolean buyOneGetOne;
    private Long shopId;
    private String shopName;
    private Double rating;
    private Integer reviewCount;
}
