package com.tribiz.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    @NotNull
    private Long serviceId;

    @NotNull
    private LocalDateTime appointmentTime;

    private String note;
}
