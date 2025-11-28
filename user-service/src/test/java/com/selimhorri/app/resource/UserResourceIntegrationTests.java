package com.selimhorri.app.resource;

import com.selimhorri.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.config.enabled=false",
        "spring.config.import="
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserResourceIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_returnsOk() throws Exception {
        BDDMockito.given(userService.findAll()).willReturn(Collections.emptyList());
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }
}
