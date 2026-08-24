package com.acmecorp.orders;

public class OrderController {

    // Correlation id received, then never forwarded to the outbound call --
    // flagged.
    void createOrder(@RequestHeader("X-Correlation-Id") String correlationId, @RequestBody String payload) {
        restTemplate.postForObject("http://inventory/reserve", payload, Void.class);
    }

    // Correlation id received and forwarded on the outbound request -- not
    // flagged.
    void cancelOrder(@RequestHeader("X-Correlation-Id") String correlationId, @RequestBody String payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-Id", correlationId);
        restTemplate.postForObject("http://inventory/release", new HttpEntity<>(payload, headers), Void.class);
    }
}
