package com.escravosdev.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final DataSource dS;

    public HealthController(DataSource dS) {
        this.dS = dS;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health()
    {
        var db = checkDatabase();
        var status = db ? "UP" : "DEGRADED";

        return ResponseEntity.ok(Map.of(
                "status", status,
                "timestamp", Instant.now().toString(),
                "services", Map.of(
                        "api", "UP",
                        "database", db ? "UP" : "DOWN"
                )
        ));
    }

    private boolean checkDatabase() {
        try (var conn = dS.getConnection()) {
            return conn.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}
