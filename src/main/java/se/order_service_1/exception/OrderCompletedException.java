package se.order_service_1.exception;

public class OrderCompletedException extends RuntimeException {
    public OrderCompletedException(String message) {
        super(message);
    }
}
