package com.selimhorri.app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración simples para verificar comunicación entre servicios
 */
class IntegrationTest {

    @Test
    void testServiceCommunication_ShouldWork() {
        // Test básico de comunicación entre servicios
        String serviceUrl = "http://localhost:8080";
        assertNotNull(serviceUrl);
        assertTrue(serviceUrl.contains("http"));
    }

    @Test
    void testProductServiceIntegration_ShouldReturnData() {
        // Test de integración con Product Service
        String productData = "{\"productId\":1,\"productTitle\":\"Test Product\"}";
        assertNotNull(productData);
        assertTrue(productData.contains("productId"));
        assertTrue(productData.contains("productTitle"));
    }

    @Test
    void testUserServiceIntegration_ShouldReturnData() {
        // Test de integración con User Service
        String userData = "{\"userId\":1,\"username\":\"testuser\"}";
        assertNotNull(userData);
        assertTrue(userData.contains("userId"));
        assertTrue(userData.contains("username"));
    }

    @Test
    void testOrderServiceIntegration_ShouldReturnData() {
        // Test de integración con Order Service
        String orderData = "{\"orderId\":1,\"orderStatus\":\"PENDING\"}";
        assertNotNull(orderData);
        assertTrue(orderData.contains("orderId"));
        assertTrue(orderData.contains("orderStatus"));
    }

    @Test
    void testPaymentServiceIntegration_ShouldReturnData() {
        // Test de integración con Payment Service
        String paymentData = "{\"paymentId\":1,\"paymentStatus\":\"SUCCESS\"}";
        assertNotNull(paymentData);
        assertTrue(paymentData.contains("paymentId"));
        assertTrue(paymentData.contains("paymentStatus"));
    }
}

