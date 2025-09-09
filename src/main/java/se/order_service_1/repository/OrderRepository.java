package se.order_service_1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.order_service_1.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
