package com.selimhorri.app.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.exception.wrapper.PaymentNotFoundException;
import com.selimhorri.app.helper.PaymentMappingHelper;
import com.selimhorri.app.repository.PaymentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private PaymentDto paymentDto;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        orderDto = OrderDto.builder()
                .orderId(5)
                .orderDesc("Test order")
                .orderFee(100.0)
                .build();

        payment = Payment.builder()
                .paymentId(1)
                .orderId(5)
                .isPayed(false)
                .paymentStatus(PaymentStatus.NOT_STARTED)
                .build();

        paymentDto = PaymentMappingHelper.map(payment);
    }

    // ---------------------------------------------------------------
    @Test
    void testFindAll_Success() {
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/" + payment.getOrderId(),
                OrderDto.class)).thenReturn(orderDto);

        List<PaymentDto> result = paymentService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderDto.getOrderId(), result.get(0).getOrderDto().getOrderId());
        assertEquals(payment.getPaymentStatus(), result.get(0).getPaymentStatus());
        verify(paymentRepository, times(1)).findAll();
        verify(restTemplate, times(1))
                .getForObject(contains(String.valueOf(payment.getOrderId())), eq(OrderDto.class));
    }

    // ---------------------------------------------------------------
    @Test
    void testFindById_Success() {
        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));

        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/" + payment.getOrderId(),
                OrderDto.class)).thenReturn(orderDto);

        PaymentDto result = paymentService.findById(1);

        assertNotNull(result);
        assertEquals(payment.getPaymentId(), result.getPaymentId());
        assertEquals(orderDto.getOrderId(), result.getOrderDto().getOrderId());
        verify(paymentRepository, times(1)).findById(1);
        verify(restTemplate, times(1))
                .getForObject(contains(String.valueOf(payment.getOrderId())), eq(OrderDto.class));
    }

    // ---------------------------------------------------------------
    @Test
    void testFindById_NotFound() {
        when(paymentRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(PaymentNotFoundException.class, () -> paymentService.findById(99));
        verify(paymentRepository, times(1)).findById(99);
    }

    // ---------------------------------------------------------------
    @Test
    void testSave_Success() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentDto inputDto = PaymentDto.builder()
                .isPayed(false)
                .paymentStatus(PaymentStatus.NOT_STARTED)
                .orderDto(OrderDto.builder().orderId(5).build())
                .build();

        PaymentDto result = paymentService.save(inputDto);

        assertNotNull(result);
        assertEquals(payment.getPaymentId(), result.getPaymentId());
        assertEquals(payment.getOrderId(), result.getOrderDto().getOrderId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    // ---------------------------------------------------------------
    @Test
    void testUpdate_Success() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentDto toUpdate = PaymentDto.builder()
                .paymentId(1)
                .isPayed(true)
                .paymentStatus(PaymentStatus.COMPLETED)
                .orderDto(OrderDto.builder().orderId(5).build())
                .build();

        PaymentDto result = paymentService.update(toUpdate);

        assertNotNull(result);
        assertEquals(toUpdate.getPaymentId(), result.getPaymentId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    // ---------------------------------------------------------------
    @Test
    void testDeleteById_Success() {
        doNothing().when(paymentRepository).deleteById(1);

        paymentService.deleteById(1);

        verify(paymentRepository, times(1)).deleteById(1);
    }
}
