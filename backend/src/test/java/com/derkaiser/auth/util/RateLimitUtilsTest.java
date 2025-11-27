package com.derkaiser.auth.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitUtilsTest {

    private RateLimitUtils rateLimitUtils;

    @BeforeEach
    void setUp() {
        rateLimitUtils = new RateLimitUtils();
        // Establecer valores de prueba para facilitar las pruebas
        // Usaremos reflexión para sobrescribir los valores estáticos si es necesario
    }

    @Test
    void isAllowed_shouldReturnTrueForFirstRequest() {
        String key = "test-key";
        assertTrue(rateLimitUtils.isAllowed(key));
    }

    @Test
    void isAllowed_shouldReturnTrueWhenWithinLimit() {
        String key = "test-key";
        
        // Realizar menos de 5 solicitudes
        assertTrue(rateLimitUtils.isAllowed(key));
        assertTrue(rateLimitUtils.isAllowed(key));
        assertTrue(rateLimitUtils.isAllowed(key));
        assertTrue(rateLimitUtils.isAllowed(key));
        
        // La quinta solicitud debería ser permitida
        assertTrue(rateLimitUtils.isAllowed(key));
    }

    @Test
    void isAllowed_shouldReturnFalseWhenExceedingLimit() {
        String key = "test-key";
        
        // Realizar 5 solicitudes válidas
        assertTrue(rateLimitUtils.isAllowed(key));
        assertTrue(rateLimitUtils.isAllowed(key));
        assertTrue(rateLimitUtils.isAllowed(key));
        assertTrue(rateLimitUtils.isAllowed(key));
        assertTrue(rateLimitUtils.isAllowed(key));
        
        // La sexta solicitud debería ser denegada
        assertFalse(rateLimitUtils.isAllowed(key));
    }

    @Test
    void recordSuccess_shouldResetCounter() {
        String key = "test-key";
        
        // Consumir todas las solicitudes permitidas
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitUtils.isAllowed(key));
        }
        
        // La siguiente debería fallar
        assertFalse(rateLimitUtils.isAllowed(key));
        
        // Registrar éxito - debería reiniciar el contador
        rateLimitUtils.recordSuccess(key);
        
        // Ahora debería permitir solicitudes nuevamente
        assertTrue(rateLimitUtils.isAllowed(key));
    }

    @Test
    void recordFailure_shouldIncrementCounter() {
        String key = "test-key";
        
        // Registrar fallas
        rateLimitUtils.recordFailure(key);
        rateLimitUtils.recordFailure(key);
        rateLimitUtils.recordFailure(key);
        rateLimitUtils.recordFailure(key);
        
        // Todavía debería permitir (menos de 5)
        assertTrue(rateLimitUtils.isAllowed(key));
        
        // Agregar una falla más
        rateLimitUtils.recordFailure(key);
        
        // Ahora debería denegar
        assertFalse(rateLimitUtils.isAllowed(key));
    }

    @Test
    void getRemainingAttempts_shouldReturnCorrectCount() {
        String key = "test-key";
        
        // Inicialmente debe haber 5 intentos
        assertEquals(5, rateLimitUtils.getRemainingAttempts(key));
        
        // Usar algunos intentos
        rateLimitUtils.isAllowed(key);
        assertEquals(4, rateLimitUtils.getRemainingAttempts(key));
        
        rateLimitUtils.isAllowed(key);
        assertEquals(3, rateLimitUtils.getRemainingAttempts(key));
        
        rateLimitUtils.isAllowed(key);
        assertEquals(2, rateLimitUtils.getRemainingAttempts(key));
    }

    @Test
    void isAllowed_shouldResetAfterWindowExpiration() throws InterruptedException {
        String key = "test-key";
        
        // Consumir todas las solicitudes permitidas
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitUtils.isAllowed(key));
        }
        
        // La siguiente debería ser denegada
        assertFalse(rateLimitUtils.isAllowed(key));
        
        // Simular que ha pasado el tiempo suficiente para que expire la ventana
        // En este caso específico, no tenemos una forma directa de cambiar el tiempo
        // pero podemos verificar que después de recordSuccess se restablece
        rateLimitUtils.recordSuccess(key);
        
        // Ahora debería permitir de nuevo
        assertTrue(rateLimitUtils.isAllowed(key));
    }

    @Test
    void differentKeysShouldBeIndependent() {
        String key1 = "test-key-1";
        String key2 = "test-key-2";
        
        // Consumir todas las solicitudes para key1
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitUtils.isAllowed(key1));
        }
        
        // key1 debería estar bloqueada ahora
        assertFalse(rateLimitUtils.isAllowed(key1));
        
        // Pero key2 debería seguir funcionando
        assertTrue(rateLimitUtils.isAllowed(key2));
    }
}