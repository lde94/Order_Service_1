package se.order_service_1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import se.order_service_1.model.OrderItem;
import se.order_service_1.model.Order;
import se.order_service_1.repository.OrderItemRepository;
import se.order_service_1.repository.OrderRepository;

import java.util.List;


@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public void addOrderItem(Long orderId, Long productId, int quantity) {
        log.debug("addOrderItem - försök lägga till produkt {} (qty={}) till orderId={}",
                productId, quantity, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("addOrderItem - Order med id={} hittades inte", orderId);
                    return new RuntimeException("Order not found"); //TODO: byt till orderNotFoundException
                });

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        log.debug("addOrderItem - orderId={} har {} items före uppdatering", orderId, items.size());

        for(OrderItem item : items){

            if(item.getProductId().equals(productId)){
                log.info("addOrderItem - produkt {} finns redan i orderId={}, uppdaterar qty {} -> {}",
                        productId, orderId, item.getQuantity(), item.getQuantity() + quantity);

                item.setQuantity(item.getQuantity() + quantity);
                orderItemRepository.save(item);
                log.debug("addOrderItem - orderId={} sparad efter uppdatering", orderId);
                return;
            }
        }

        OrderItem newItem = new OrderItem();
        newItem.setProductId(productId);
        newItem.setOrder(order);
        newItem.setQuantity(quantity);
        orderItemRepository.save(newItem);

        log.info("addOrderItem - produkt {} tillagd i orderId={} med qty={}",
                newItem.getProductId(), orderId, quantity);

        log.debug("addOrderItem - orderId={} sparad med nytt item, total items={}", orderId, items.size());

    }

    public List<OrderItem> getOrderItems(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return items;
    }

    public List<Order> getAllOrders() {
        log.debug("getAllOrders - hämta alla ordrar");
        List<Order> orders = orderRepository.findAll();
        log.debug("getAllOrders - antal ordrar={}", orders.size());
        return orders;
    }

    public void deleteOrder(Long orderId) {
        log.info("deleteOrder - försök radera order med id={}", orderId);
        if(orderRepository.existsById(orderId)){
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            orderItemRepository.deleteAll(items);
//            orderItemRepository.deleteAllByOrderId(orderId);
            orderRepository.deleteById(orderId);
            log.info("deleteOrder - order raderad med id={}", orderId);
        } else {
            log.warn("deleteOrder - ingen order att radera med id={}", orderId);
        }
    }

    public Order getOrderById(Long orderId) {
        log.debug("getOrderById - försök hämta order med id={}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("getOrderById - ingen order hittades med id={}", orderId);
                    return new RuntimeException("Order med ID " + orderId + " finns inte"); //TODO: byt till orderNotFoundException
                });
        log.debug("getOrderById - hittade order={}", orderId);
        return order;
    }

    public List<Order> getOrdersByUser(Long userId) {
        log.debug("getOrdersByUser - försök hämta alla ordrar från använder med id={}", userId);
        List <Order> orders = orderRepository.findOrdersByUserId(userId);
        return orders;
    }

    public Order updateOrder(Long orderID, Long productID, Integer quantity) {
        //TODO: Skapa update funkionalitet
        Order order = getOrderById(orderID);
        List<OrderItem> orderItems = getOrderItems(orderID);
        for(OrderItem orderItem : orderItems){
            if(orderItem.getProductId().equals(productID)){
                orderItem.setQuantity(quantity);
                orderItemRepository.save(orderItem);
                return order;
            }
        }
        addOrderItem(orderID, productID, quantity);
        return order;
    }

    public void finalizeOrder(Long orderId) {
        Order order = getOrderById(orderId);
        order.setOrderStatus(Order.OrderStatus.COMPLETED);
        orderRepository.save(order);
        //TODO: uppdatera ProductService stockQuantity och kolla att stockQuantity inte går under 0
    }

    // Temporär funktion för testning
    public Order createOrder(Long userId){
        Order order = Order.builder()
                .userId(userId)
                .orderStatus(Order.OrderStatus.ONGOING)
                .build();
        return orderRepository.save(order);
    }

}
