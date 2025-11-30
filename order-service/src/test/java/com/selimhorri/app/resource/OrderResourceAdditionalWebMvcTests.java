package com.selimhorri.app.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderResource.class)
@TestPropertySource(properties = {
        "spring.config.import=",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class OrderResourceAdditionalWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderDto sampleOrder() {
        return OrderDto.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("test order")
                .orderFee(12.3)
                .cartDto(CartDto.builder().cartId(1).build())
                .build();
    }

    @Test
    void getOrderById_returnsOk() throws Exception {
        BDDMockito.given(orderService.findById(1)).willReturn(sampleOrder());
        mockMvc.perform(get("/api/orders/{id}", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderById_withBlank_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", " "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_returnsOk() throws Exception {
        var dto = sampleOrder();
        dto.setOrderId(null);
        BDDMockito.given(orderService.save(ArgumentMatchers.any(OrderDto.class)))
                .willReturn(sampleOrder());
        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateOrder_returnsOk() throws Exception {
        var dto = sampleOrder();
        BDDMockito.given(orderService.update(ArgumentMatchers.any(OrderDto.class)))
                .willReturn(dto);
        mockMvc.perform(put("/api/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteOrder_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", "1"))
                .andExpect(status().isOk());
    }
}

