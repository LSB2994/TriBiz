package com.tribiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponse {
    private Long id;
    private String name;
    private String location;
    private String contactInfo;
    private Boolean isOpen;
    private Double rating;
    private Integer reviewCount;
    private Double latitude;
    private Double longitude;
}
