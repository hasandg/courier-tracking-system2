package com.hasandag.courier.tracking.system.distancecalculation.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierLocationDto {
    private Long id;
    private String courierId;
    private double latitude;
    private double longitude;
    private LocalDateTime timestamp;
}
