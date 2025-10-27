package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.domain.Order;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Cart cart = Cart.builder()
                .cartId(1)
                .userId(100)
                .build();

        order = Order.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Test Order")
                .orderFee(5000.0)
                .cart(cart)
                .build();

        orderDto = OrderDto.builder()
                .orderId(order.getOrderId())
                .orderDate(order.getOrderDate())
                .orderDesc(order.getOrderDesc())
                .orderFee(order.getOrderFee())
                .cartDto(CartDto.builder()
                        .cartId(cart.getCartId())
                        .userId(cart.getUserId())
                        .build())
                .build();
    }

    @Test
    void testFindAll_Positive() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderDto> result = orderService.findAll();

        assertEquals(1, result.size());
        assertEquals(order.getOrderId(), result.get(0).getOrderId());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Positive() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        OrderDto result = orderService.findById(1);

        assertEquals(order.getOrderId(), result.getOrderId());
        assertEquals(order.getOrderDesc(), result.getOrderDesc());

        verify(orderRepository, times(1)).findById(1);
    }

    @Test
    void testSave_Positive() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDto result = orderService.save(orderDto);

        assertEquals(order.getOrderId(), result.getOrderId());
        assertEquals(order.getOrderDesc(), result.getOrderDesc());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdateByDto_Positive() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDto result = orderService.update(orderDto);

        assertEquals(order.getOrderId(), result.getOrderId());
        assertEquals(order.getOrderDesc(), result.getOrderDesc());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdateById_Positive() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDto result = orderService.update(1, orderDto);

        assertEquals(order.getOrderId(), result.getOrderId());
        assertEquals(order.getOrderDesc(), result.getOrderDesc());

        verify(orderRepository, times(1)).findById(1);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testDeleteById_Positive() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        orderService.deleteById(1);

        verify(orderRepository, times(1)).delete(any(Order.class));
    }
}
