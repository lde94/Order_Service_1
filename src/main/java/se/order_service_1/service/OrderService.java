package se.order_service_1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.order_service_1.dto.PlaceOrderRequest;
import se.order_service_1.exception.NotEnoughStockException;
import se.order_service_1.exception.OrderCompletedException;
import se.order_service_1.exception.OrderNotFoundException;
import se.order_service_1.exception.ProductNotFoundException;
import se.order_service_1.model.OrderItem;
import se.order_service_1.model.Order;
import se.order_service_1.repository.OrderItemRepository;
import se.order_service_1.repository.OrderRepository;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestTemplate restTemplate;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.restTemplate = restTemplate;
    }

    public void addOrderItem(Long orderId, Long productId, int quantity) {
        log.debug("addOrderItem - försök lägga till produkt {} (qty={}) till orderId={}",
                productId, quantity, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("addOrderItem - Order med id={} hittades inte", orderId);
                    return new RuntimeException("Order not found"); //TODO: byt till orderNotFoundException
                });

        checkNotCompleted(order);

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
            Order order = getOrderById(orderId);
            checkNotCompleted(order);
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            orderItemRepository.deleteAll(items);
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
                    return new OrderNotFoundException("Order med ID " + orderId + " finns inte");
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
        Order order = getOrderById(orderID);
        checkNotCompleted(order);
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
        checkNotCompleted(order);

        //TODO: uppdatera ProductService stockQuantity
        List<PlaceOrderRequest.ProductChange> productChangeList = new ArrayList<>();
        PlaceOrderRequest.ProductChange productChange;
        for(OrderItem orderItem : orderItemRepository.findByOrderId(orderId)){
            productChange = PlaceOrderRequest.ProductChange.builder()
                    .productId(orderItem.getProductId())
                    .inventoryChange(-orderItem.getQuantity())
                    .build();
            productChangeList.add(productChange);
        }
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(productChangeList);
        String url = "http://localhost:8080/product/inventoryManager";
        String uri = "/product/inventoryManager";

        //Check send change to product-service and see if it can be changed

        RestTemplate restTemplate = new RestTemplate();
        //TODO Handle exception thrown from 400 status code
        ResponseEntity<String> response = restTemplate.postForEntity(url, placeOrderRequest, String.class);

        order.setOrderStatus(Order.OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    // Temporär funktion för testning
    public Order createOrder(Long userId){
        Order order = Order.builder()
                .userId(userId)
                .orderStatus(Order.OrderStatus.ONGOING)
                .build();
        return orderRepository.save(order);
    }

    private void checkNotCompleted(Order order) {
        if (order.getOrderStatus() == Order.OrderStatus.COMPLETED) {
            throw new OrderCompletedException("Order is completed and can not be changed or cancelled");
        }
    }

}
