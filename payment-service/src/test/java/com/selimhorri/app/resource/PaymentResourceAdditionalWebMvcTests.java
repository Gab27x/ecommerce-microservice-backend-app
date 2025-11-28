package com.selimhorri.app.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.service.PaymentService;
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

@WebMvcTest(PaymentResource.class)
@TestPropertySource(properties = {
        "spring.config.import=",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class PaymentResourceAdditionalWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentDto samplePayment() {
        return PaymentDto.builder()
                .paymentId(1)
                .isPayed(true)
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();
    }

    @Test
    void getPaymentById_returnsOk() throws Exception {
        BDDMockito.given(paymentService.findById(1)).willReturn(samplePayment());
        mockMvc.perform(get("/api/payments/{id}", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentById_withBlank_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/payments/{id}", " "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPayment_returnsOk() throws Exception {
        var dto = samplePayment();
        dto.setPaymentId(null);
        BDDMockito.given(paymentService.save(ArgumentMatchers.any(PaymentDto.class)))
                .willReturn(samplePayment());
        mockMvc.perform(post("/api/payments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePayment_returnsOk() throws Exception {
        var dto = samplePayment();
        BDDMockito.given(paymentService.update(ArgumentMatchers.any(PaymentDto.class)))
                .willReturn(dto);
        mockMvc.perform(put("/api/payments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deletePayment_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/payments/{id}", "1"))
                .andExpect(status().isOk());
    }
}

