package se.order_service_1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {
    private String kortNummer;
    private String kortInnehavare;
    private String utgangsdatum; // Format: MM/ÅÅ
    private String cvv;
    private Double belopp;
}