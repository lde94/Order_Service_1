package se.order_service_1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.order_service_1.model.OrderItem;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
}
