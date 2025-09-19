package se.order_service_1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.order_service_1.model.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findOrdersByUserId(Long userId);
    List<Order> findByUserIdAndOrderDateAfter(Long userId, LocalDateTime orderDate);
}
