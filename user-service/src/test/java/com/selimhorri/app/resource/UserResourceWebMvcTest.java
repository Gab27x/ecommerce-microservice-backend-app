package com.selimhorri.app.resource;

import com.selimhorri.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserResource.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.config.import=",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class UserResourceWebMvcTest {

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
