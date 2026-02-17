package com.tribiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShopRequest {
    @NotBlank
    private String name;

    private String location;

    private String contactInfo;
}
