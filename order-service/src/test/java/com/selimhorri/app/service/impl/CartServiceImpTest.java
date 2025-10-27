package com.selimhorri.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.CartNotFoundException;
import com.selimhorri.app.helper.CartMappingHelper;
import com.selimhorri.app.repository.CartRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartDto cartDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .build();

        cartDto = CartDto.builder()
                .cartId(1)
                .userId(1)
                .orderDtos(Set.of())
                .userDto(userDto)
                .build();
    }

    @Test
    void testFindAll() {
        when(cartRepository.findAll()).thenReturn(List.of(CartMappingHelper.map(cartDto)));
        when(restTemplate.getForObject(anyString(), eq(UserDto.class))).thenReturn(userDto);

        List<CartDto> result = cartService.findAll();

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getUserDto().getUserId()).isEqualTo(1);
        verify(cartRepository).findAll();
        verify(restTemplate).getForObject(anyString(), eq(UserDto.class));
    }

    @Test
    void testFindById_Found() {
        when(cartRepository.findById(1)).thenReturn(Optional.of(CartMappingHelper.map(cartDto)));
        when(restTemplate.getForObject(anyString(), eq(UserDto.class))).thenReturn(userDto);

        CartDto result = cartService.findById(1);

        assertThat(result).isNotNull();
        assertThat(result.getCartId()).isEqualTo(1);
    }

    @Test
    void testFindById_NotFound() {
        when(cartRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartService.findById(1));
    }

    @Test
    void testSave() {
        when(cartRepository.save(any())).thenReturn(CartMappingHelper.map(cartDto));

        CartDto result = cartService.save(cartDto);

        assertThat(result).isNotNull();
        assertThat(result.getCartId()).isEqualTo(1);
    }

    @Test
    void testUpdate() {
        when(cartRepository.save(any())).thenReturn(CartMappingHelper.map(cartDto));

        CartDto result = cartService.update(cartDto);

        assertThat(result).isNotNull();
        assertThat(result.getCartId()).isEqualTo(1);
    }

    @Test
    void testDeleteById() {
        doNothing().when(cartRepository).deleteById(1);

        cartService.deleteById(1);

        verify(cartRepository, times(1)).deleteById(1);
    }
}
