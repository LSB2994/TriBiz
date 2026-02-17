package com.tribiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private BigDecimal price;

    private Integer quantity;

    private String image;

    private String status;

    private String barcode;

    private String category;

    private BigDecimal discount;

    private Boolean buyOneGetOne;
}
