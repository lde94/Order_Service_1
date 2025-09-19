package se.order_service_1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderHistoryRequest {
    LocalDateTime earliestOrderDate;
}
