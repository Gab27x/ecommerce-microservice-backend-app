package com.selimhorri.app.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.service.OrderItemService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderItemResource.class)
@TestPropertySource(properties = {
        "spring.config.import=",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class OrderItemResourceAdditionalWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderItemService orderItemService;

    private OrderItemDto sampleItem() {
        return OrderItemDto.builder()
                .orderId(1)
                .productId(2)
                .orderedQuantity(3)
                .build();
    }

    @Test
    void getAllOrderItems_returnsOk() throws Exception {
        mockMvc.perform(get("/api/shippings"))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderItemById_returnsOk() throws Exception {
        BDDMockito.given(orderItemService.findById(ArgumentMatchers.any(OrderItemId.class)))
                .willReturn(sampleItem());
        mockMvc.perform(get("/api/shippings/{orderId}/{productId}", "1", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void createOrderItem_returnsOk() throws Exception {
        var dto = sampleItem();
        BDDMockito.given(orderItemService.save(ArgumentMatchers.any(OrderItemDto.class)))
                .willReturn(dto);
        mockMvc.perform(post("/api/shippings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateOrderItem_returnsOk() throws Exception {
        var dto = sampleItem();
        BDDMockito.given(orderItemService.update(ArgumentMatchers.any(OrderItemDto.class)))
                .willReturn(dto);
        mockMvc.perform(put("/api/shippings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteOrderItem_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/shippings/{orderId}/{productId}", "1", "2"))
                .andExpect(status().isOk());
    }
}

