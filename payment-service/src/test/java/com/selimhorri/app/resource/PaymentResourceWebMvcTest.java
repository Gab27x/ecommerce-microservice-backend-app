package com.selimhorri.app.resource;

import com.selimhorri.app.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentResource.class)
class PaymentResourceWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    void getAllPayments_returnsOk() throws Exception {
        BDDMockito.given(paymentService.findAll()).willReturn(Collections.emptyList());
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk());
    }
}
