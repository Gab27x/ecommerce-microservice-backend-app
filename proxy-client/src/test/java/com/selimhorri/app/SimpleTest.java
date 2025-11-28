package com.selimhorri.app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple para verificar que JUnit funciona correctamente
 */
class SimpleTest {

    @Test
    void testBasicMath() {
        // Test básico de matemáticas
        assertEquals(4, 2 + 2);
        assertTrue(5 > 3);
        assertFalse(1 > 2);
    }

    @Test
    void testStringOperations() {
        // Test básico de strings
        String hello = "Hello";
        String world = "World";
        String result = hello + " " + world;
        
        assertEquals("Hello World", result);
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("World"));
    }

    @Test
    void testArrayOperations() {
        // Test básico de arrays
        int[] numbers = {1, 2, 3, 4, 5};
        
        assertEquals(5, numbers.length);
        assertEquals(1, numbers[0]);
        assertEquals(5, numbers[4]);
    }

    @Test
    void testBooleanLogic() {
        // Test básico de lógica booleana
        boolean isTrue = true;
        boolean isFalse = false;
        
        assertTrue(isTrue);
        assertFalse(isFalse);
        assertTrue(isTrue || isFalse);
        assertFalse(isTrue && isFalse);
    }

    @Test
    void testNullChecks() {
        // Test básico de null checks
        String nullString = null;
        String notNullString = "test";
        
        assertNull(nullString);
        assertNotNull(notNullString);
    }
}

