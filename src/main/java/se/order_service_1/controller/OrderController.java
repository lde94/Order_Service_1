package se.order_service_1.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.order_service_1.dto.OrderItemRespons;
import se.order_service_1.dto.OrderRequest;
import se.order_service_1.dto.OrderResponse;
import se.order_service_1.model.Order;
import se.order_service_1.model.OrderItem;
import se.order_service_1.service.OrderService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order")
@AllArgsConstructor
public class OrderController {
    private OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        OrderResponse orderResponse = createOrderResponse(order);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/orderHistory/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrderHistory(@PathVariable Long userId) {
        List<Order> orderList = orderService.getOrdersByUser(userId);
        List<OrderResponse> orderResponseList = new ArrayList<>();
        OrderResponse orderResponse;
        for (Order order : orderList) {
            orderResponse = createOrderResponse(order);
            orderResponseList.add(orderResponse);
        }
        return ResponseEntity.ok(orderResponseList);
    }
    /*Temporär PostMapping för att testa orderService individuellt */
    private static long tempUserId = 0L;
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder() {
        tempUserId++;
        Order order = orderService.createOrder(tempUserId);
        OrderResponse orderResponse = createOrderResponse(order);
        return ResponseEntity.ok(orderResponse);
    }

    @PostMapping("/addToOrder")
    public ResponseEntity<OrderResponse> addToOrder(@RequestBody OrderRequest orderRequest) {
        //TODO vad händer om en order aldrig skapats/ när skapas en ny order??? Måste ha en user att koppla med först
        Order order = orderService.getOrderById(orderRequest.getOrderId());
        if (order.getOrderStatus() == Order.OrderStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); //Heja heja Alexander! :D
        }
        orderService.addOrderItem(orderRequest.getOrderId(), orderRequest.getProductId(), orderRequest.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(createOrderResponse(order));
    }

    @PutMapping("/finalizeOrder/{orderId}")
    public ResponseEntity<String> finalizeOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order.getOrderStatus() == Order.OrderStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        orderService.finalizeOrder(orderId);
        return ResponseEntity.ok("Order has been finalized");
    }

    @PutMapping("/update")
    public ResponseEntity<OrderResponse> updateOrder(@RequestBody OrderRequest orderRequest) {
        //TODO update only if in ongoing phase
        //TODO update quantity for an OrderItem
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(null);
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@RequestBody OrderRequest orderRequest) {
        //TODO delete order only if still in ongoing phase
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(null);
    }

    private OrderResponse createOrderResponse(Order order) {
        List<OrderItem> orderItemList = orderService.getOrderItems(order.getId());
        List<OrderItemRespons> orderItemResponsList = new ArrayList<>();
        for (OrderItem orderItem : orderItemList) {
            OrderItemRespons orderItemRespons = OrderItemRespons.builder()
                    .productId(orderItem.getProductId())
                    .quantity(orderItem.getQuantity())
                    .build();
            orderItemResponsList.add(orderItemRespons);
        }
        return OrderResponse.builder()
                .OrderId(order.getId())
                .items(orderItemResponsList)
                .orderStatus(order.getOrderStatus())
                .build();
    }
}
