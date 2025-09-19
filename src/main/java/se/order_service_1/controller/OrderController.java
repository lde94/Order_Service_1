package se.order_service_1.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.order_service_1.dto.*;
import se.order_service_1.model.Order;
import se.order_service_1.model.OrderItem;
import se.order_service_1.service.OrderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    @PostMapping("/orderHistory")
    public ResponseEntity<List<OrderResponse>> getOrderHistory(@RequestBody OrderHistoryRequest orderHistoryRequest) {
        List<Order> orderList;
        if(orderHistoryRequest.getEarliestOrderDate() == null) {
             orderList = orderService.getOrdersByUser(orderHistoryRequest.getUserId());
        } else {
            orderList = orderService.getOrdersAfterOrderDate(orderHistoryRequest.getUserId(), orderHistoryRequest.getEarliestOrderDate());
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        OrderResponse orderResponse;
        for (Order order : orderList) {
            orderResponse = createOrderResponse(order);
            orderResponseList.add(orderResponse);
        }
        return ResponseEntity.ok(orderResponseList);
    }

    /*TODO Temporär PostMapping för att testa orderService individuellt */
    private final static long tempUserId = 1L;
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder() {
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

    @Operation(
            summary = "Finalize order",
            description = "Slutför en pågående order genom att behandla betalning och ändra orderstatus"
    )
    @PutMapping("/finalizeOrder/{orderId}")
    public ResponseEntity<Map<String, String>> finalizeOrder(
            @PathVariable Long orderId,
            @RequestBody FinalizeOrderRequest request) {

        // Behandla betalning och slutför ordern
        String transactionId = orderService.finalizeOrder(orderId, request.getBetalningsuppgifter());

        // Returnera transaktions-ID och bekräftelsemeddelande
        return ResponseEntity.ok(Map.of(
                "meddelande", "Betalning godkänd och order slutförd",
                "transaktionsId", transactionId
        ));
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
                .orderPlacedDate(order.getOrderDate())
                .build();
    }
}