package se.order_service_1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.order_service_1.model.Order;
import se.order_service_1.model.OrderItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    long OrderId;
    Order.OrderStatus orderStatus;
    LocalDateTime orderPlacedDate;
    List<OrderItemRespons> items;
}
