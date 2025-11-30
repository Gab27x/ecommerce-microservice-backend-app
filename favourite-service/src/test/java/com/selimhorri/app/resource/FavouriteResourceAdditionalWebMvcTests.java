package com.selimhorri.app.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.service.FavouriteService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavouriteResource.class)
@TestPropertySource(properties = {
        "spring.config.import=",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class FavouriteResourceAdditionalWebMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FavouriteService favouriteService;

    private FavouriteDto sampleFav() {
        return FavouriteDto.builder()
                .userId(1)
                .productId(2)
                .likeDate(LocalDateTime.now())
                .build();
    }

    private String likeDateStr(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ofPattern(AppConstant.LOCAL_DATE_TIME_FORMAT));
    }

    @Test
    void getAllFavourites_returnsOk() throws Exception {
        mockMvc.perform(get("/api/favourites"))
                .andExpect(status().isOk());
    }

    @Test
    void getFavouriteById_returnsOk() throws Exception {
        var dto = sampleFav();
        BDDMockito.given(favouriteService.findById(ArgumentMatchers.any(FavouriteId.class)))
                .willReturn(dto);
        mockMvc.perform(get("/api/favourites/{userId}/{productId}/{likeDate}",
                        "1", "2", likeDateStr(dto.getLikeDate())))
                .andExpect(status().isOk());
    }

    @Test
    void createFavourite_returnsOk() throws Exception {
        var dto = sampleFav();
        BDDMockito.given(favouriteService.save(ArgumentMatchers.any(FavouriteDto.class)))
                .willReturn(dto);
        mockMvc.perform(post("/api/favourites")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateFavourite_returnsOk() throws Exception {
        var dto = sampleFav();
        BDDMockito.given(favouriteService.update(ArgumentMatchers.any(FavouriteDto.class)))
                .willReturn(dto);
        mockMvc.perform(put("/api/favourites")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFavourite_returnsOk() throws Exception {
        var dto = sampleFav();
        mockMvc.perform(delete("/api/favourites/{userId}/{productId}/{likeDate}",
                        "1", "2", likeDateStr(dto.getLikeDate())))
                .andExpect(status().isOk());
    }
}

