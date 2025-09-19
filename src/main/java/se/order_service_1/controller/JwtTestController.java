package se.order_service_1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class JwtTestController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello(Authentication authentication) {
        // Authentication will only be populated if JWT was valid
        String username = (authentication != null) ? authentication.getName() : "anonymous";
        return ResponseEntity.ok("Hello, " + username + "! Your token is valid.");
    }
}
