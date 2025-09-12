package se.order_service_1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceOrderRequest {
    List<ProductChange> inventoryChanges;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProductChange {
        private Long productId;
        private Integer inventoryChange;
    }
}
