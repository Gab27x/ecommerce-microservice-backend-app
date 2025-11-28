package com.selimhorri.app.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.service.UserService;
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

@WebMvcTest(UserResource.class)
@TestPropertySource(properties = {
        "spring.config.import=",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class UserResourceAdditionalWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto sampleUser() {
        return UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .imageUrl("http://img")
                .email("john@doe.test")
                .phone("123456")
                .credentialDto(CredentialDto.builder().credentialId(1).username("john").password("pass").isEnabled(true).build())
                .build();
    }

    @Test
    void getAllUsers_returnsOk() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_withBlank_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/{id}", " "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_returnsOk() throws Exception {
        var dto = sampleUser();
        dto.setUserId(null);
        BDDMockito.given(userService.save(ArgumentMatchers.any(UserDto.class)))
                .willReturn(sampleUser());
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_returnsOk() throws Exception {
        var dto = sampleUser();
        BDDMockito.given(userService.update(ArgumentMatchers.any(UserDto.class)))
                .willReturn(dto);
        mockMvc.perform(put("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", "1"))
                .andExpect(status().isOk());
    }
}
