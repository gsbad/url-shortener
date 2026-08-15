package dev.gustavosa.urlshortener.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check da aplicação.
 *
 * <p>O Actuator já expõe {@code /actuator/health}. Este endpoint existe em
 * paralelo por ser um contrato estável e próprio da aplicação: o formato do
 * Actuator pode mudar entre versões do Spring Boot, e quem consome o health
 * check (proxy, CI, orquestrador) não deveria quebrar por causa disso.
 */
@RestController
public class HealthController {

    private final String appName;
    private final String environment;

    public HealthController(
            @Value("${spring.application.name}") String appName,
            @Value("${app.environment}") String environment) {
        this.appName = appName;
        this.environment = environment;
    }

    public record Health(String status, String app, String environment) {}

    @GetMapping("/health")
    public ResponseEntity<Health> health() {
        return ResponseEntity.ok(new Health("ok", appName, environment));
    }
}
