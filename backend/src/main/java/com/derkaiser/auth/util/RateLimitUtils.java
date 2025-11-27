package com.derkaiser.auth.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitUtils {

    // Almacenamiento en memoria de los intentos por clave (IP, email, etc.)
    private final ConcurrentHashMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();
    
    // Configuración del rate limiting
    private static final int MAX_ATTEMPTS = 5;
    private static final int WINDOW_SECONDS = 300; // 5 minutos

    public boolean isAllowed(String key) {
        AttemptRecord record = attempts.get(key);
        
        if (record == null) {
            // Primera vez que se intenta con esta clave
            attempts.put(key, new AttemptRecord(1, LocalDateTime.now()));
            return true;
        }
        
        // Verificar si la ventana de tiempo ha expirado
        if (record.getTimestamp().isBefore(LocalDateTime.now().minusSeconds(WINDOW_SECONDS))) {
            // Resetear el contador si la ventana ha expirado
            attempts.put(key, new AttemptRecord(1, LocalDateTime.now()));
            return true;
        }
        
        // Verificar si no se ha alcanzado el límite
        if (record.getCount() < MAX_ATTEMPTS) {
            record.increment();
            return true;
        }
        
        // Límite alcanzado
        return false;
    }

    public void recordSuccess(String key) {
        // Limpiar el registro al tener éxito
        attempts.remove(key);
    }

    public void recordFailure(String key) {
        // Ya se incrementa en isAllowed(), pero si queremos registrar un fallo adicional
        // sin comprobar si está permitido, podemos usar este método
        AttemptRecord record = attempts.get(key);
        if (record == null) {
            attempts.put(key, new AttemptRecord(1, LocalDateTime.now()));
        } else {
            record.increment();
        }
    }

    public long getRemainingAttempts(String key) {
        AttemptRecord record = attempts.get(key);
        
        if (record == null || record.getTimestamp().isBefore(LocalDateTime.now().minusSeconds(WINDOW_SECONDS))) {
            return MAX_ATTEMPTS;
        }
        
        return Math.max(0, MAX_ATTEMPTS - record.getCount());
    }

    // Clase interna para almacenar información de intentos
    private static class AttemptRecord {
        private AtomicInteger count;
        private LocalDateTime timestamp;

        public AttemptRecord(int initialCount, LocalDateTime timestamp) {
            this.count = new AtomicInteger(initialCount);
            this.timestamp = timestamp;
        }

        public void increment() {
            this.count.incrementAndGet();
            // Actualizar timestamp para prolongar la ventana
            this.timestamp = LocalDateTime.now();
        }

        public int getCount() {
            return count.get();
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}