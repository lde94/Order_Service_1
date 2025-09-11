package se.order_service_1.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
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
    @Operation(summary = "Get order by id", description = "Get a list of products for a specific order by id")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        OrderResponse orderResponse = createOrderResponse(order);
        return ResponseEntity.ok(orderResponse);
    }
    @Operation(summary = "Get order history", description = "Get order history for a specific user by id")
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
    @Operation(summary = "Add item to order", description = "Add item to order with id and quantity")
    @PostMapping("/addToOrder")
    public ResponseEntity<OrderResponse> addToOrder(@RequestBody OrderRequest orderRequest) {
        orderService.addOrderItem(orderRequest.getOrderId(), orderRequest.getProductId(), orderRequest.getQuantity());
        Order order = orderService.getOrderById(orderRequest.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createOrderResponse(order));
    }
    @Operation(summary = "Finalize order", description = "Finalize ongoing order by orderId")
    @PutMapping("/finalizeOrder/{orderId}")
    public ResponseEntity<String> finalizeOrder(@PathVariable Long orderId) {
        orderService.finalizeOrder(orderId);
        return ResponseEntity.ok("Order has been finalized");
    }
    @Operation(summary = "Update order", description = "Update order with productId and quantity")
    @PutMapping("/update")
    public ResponseEntity<OrderResponse> updateOrder(@RequestBody OrderRequest orderRequest) {
        Order order = orderService.updateOrder(orderRequest.getOrderId(), orderRequest.getProductId(), orderRequest.getQuantity());
        return ResponseEntity.ok(createOrderResponse(order));
    }
    @Operation(summary = "Delete order", description = "Cancel order with orderId")
    @DeleteMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok("Order has been cancelled");
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
