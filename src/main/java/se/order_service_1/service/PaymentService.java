package se.order_service_1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.order_service_1.dto.PaymentRequest;
import se.order_service_1.exception.BadRequestException;

import java.util.UUID;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    /**
     * Simulerar en betalningsprocess
     * @param paymentRequest betalningsinformation
     * @return transaktions-ID om betalningen lyckades
     * @throws BadRequestException om betalningen misslyckas
     */
    public String processPayment(PaymentRequest paymentRequest) {
        log.info("Behandlar betalning på {} kr från {}",
                paymentRequest.getBelopp(), paymentRequest.getKortInnehavare());

        // Validering av kortuppgifter
        validatePaymentRequest(paymentRequest);

        // Simulera kommunikation med betalningsgateway
        simulatePaymentGateway(paymentRequest);

        // Generera och returnera ett transaktions-ID
        String transactionId = UUID.randomUUID().toString();
        log.info("Betalning godkänd med transaktions-ID: {}", transactionId);

        return transactionId;
    }

    private void validatePaymentRequest(PaymentRequest paymentRequest) {
        // Validera att kortuppgifterna är giltiga
        if (paymentRequest.getKortNummer() == null || !isValidCreditCardNumber(paymentRequest.getKortNummer())) {
            log.warn("Ogiltigt kortnummer: {}", paymentRequest.getKortNummer());
            throw new BadRequestException("Ogiltigt kortnummer");
        }

        if (paymentRequest.getUtgangsdatum() == null || !isValidExpiryDate(paymentRequest.getUtgangsdatum())) {
            log.warn("Ogiltigt utgångsdatum: {}", paymentRequest.getUtgangsdatum());
            throw new BadRequestException("Ogiltigt utgångsdatum (format: MM/ÅÅ)");
        }

        if (paymentRequest.getCvv() == null || !isValidCvv(paymentRequest.getCvv())) {
            log.warn("Ogiltig CVV-kod");
            throw new BadRequestException("Ogiltig CVV-kod");
        }

        if (paymentRequest.getBelopp() == null || paymentRequest.getBelopp() <= 0) {
            log.warn("Ogiltigt belopp: {}", paymentRequest.getBelopp());
            throw new BadRequestException("Ogiltigt belopp");
        }
    }

    private void simulatePaymentGateway(PaymentRequest paymentRequest) {
        log.debug("Simulerar kommunikation med betalningsgateway...");

        // Simulera slumpmässigt betalningsfel (ca 10% av fallen)
        if (Math.random() < 0.1) {
            log.warn("Betalning nekad: Otillräckliga medel");
            throw new BadRequestException("Betalning nekad: Otillräckliga medel");
        }

        // Simulera nätverksfördröjning för en mer realistisk upplevelse
        try {
            Thread.sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.debug("Betalningsgateway godkände transaktionen");
    }

    // Validera kortnummer med Luhn-algoritmen
    private boolean isValidCreditCardNumber(String cardNumber) {
        // Ta bort mellanslag och streck för att hantera olika format
        String cleanCardNumber = cardNumber.replaceAll("[ -]", "");

        // Enkel validering: kontrollera längd och att bara siffror används
        if (cleanCardNumber.length() < 13 || cleanCardNumber.length() > 19) {
            return false;
        }

        if (!cleanCardNumber.matches("\\d+")) {
            return false;
        }

        // För detta exempel: acceptera alla kortnummer som har rätt format
        // I en riktig implementation skulle mer omfattande validering göras
        return true;
    }

    private boolean isValidExpiryDate(String expiryDate) {
        // Kontrollera format (MM/ÅÅ)
        return expiryDate.matches("(0[1-9]|1[0-2])/[0-9]{2}");
    }

    private boolean isValidCvv(String cvv) {
        // CVV är vanligtvis 3-4 siffror
        return cvv.matches("[0-9]{3,4}");
    }
}