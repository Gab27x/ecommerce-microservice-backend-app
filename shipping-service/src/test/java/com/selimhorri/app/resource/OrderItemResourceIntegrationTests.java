package com.selimhorri.app.resource;

import com.selimhorri.app.service.OrderItemService;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false"
})
@AutoConfigureMockMvc
class OrderItemResourceIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderItemService orderItemService;

    @Test
    void getAllShippings_returnsOk() throws Exception {
        BDDMockito.given(orderItemService.findAll()).willReturn(Collections.emptyList());
        mockMvc.perform(get("/api/shippings"))
                .andExpect(status().isOk());
    }
}

