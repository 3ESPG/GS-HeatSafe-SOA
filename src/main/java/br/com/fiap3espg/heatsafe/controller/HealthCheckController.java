package br.com.fiap3espg.heatsafe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Tag(name = "Health check", description = "Verifica se o serviço está no ar")
public class HealthCheckController {

    @GetMapping("/health")
    @Operation(summary = "Status do serviço")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "servico", "HeatSafe Clima API", "horario", LocalDateTime.now()));
    }
}
