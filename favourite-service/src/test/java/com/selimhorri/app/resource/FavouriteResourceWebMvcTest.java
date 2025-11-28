package com.selimhorri.app.resource;

import com.selimhorri.app.service.FavouriteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FavouriteResource.class)
class FavouriteResourceWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavouriteService favouriteService;

    @Test
    void getAllFavourites_returnsOk() throws Exception {
        mockMvc.perform(get("/api/favourites"))
                .andExpect(status().isOk());
    }
}

