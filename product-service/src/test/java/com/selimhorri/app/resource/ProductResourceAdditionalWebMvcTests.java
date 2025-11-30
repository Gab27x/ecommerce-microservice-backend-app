package com.selimhorri.app.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.service.ProductService;
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

@WebMvcTest(ProductResource.class)
@TestPropertySource(properties = {
        "spring.config.import=",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class ProductResourceAdditionalWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductDto sampleProduct() {
        return ProductDto.builder()
                .productId(1)
                .productTitle("Sample")
                .imageUrl("http://img")
                .sku("SKU-1")
                .priceUnit(10.5)
                .quantity(5)
                .categoryDto(CategoryDto.builder().categoryId(1).categoryTitle("Cat").build())
                .build();
    }

    @Test
    void getProductById_returnsOk() throws Exception {
        BDDMockito.given(productService.findById(1)).willReturn(sampleProduct());
        mockMvc.perform(get("/api/products/{id}", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getProductById_withBlankId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/products/{id}", " "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_returnsOk() throws Exception {
        var dto = sampleProduct();
        dto.setProductId(null);
        BDDMockito.given(productService.save(ArgumentMatchers.any(ProductDto.class)))
                .willReturn(sampleProduct());
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateProduct_returnsOk() throws Exception {
        var dto = sampleProduct();
        BDDMockito.given(productService.update(ArgumentMatchers.any(ProductDto.class)))
                .willReturn(dto);
        mockMvc.perform(put("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProduct_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", "1"))
                .andExpect(status().isOk());
    }
}

