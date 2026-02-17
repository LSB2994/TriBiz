package com.tribiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String image;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private Long shopId;
    private String shopName;
}
