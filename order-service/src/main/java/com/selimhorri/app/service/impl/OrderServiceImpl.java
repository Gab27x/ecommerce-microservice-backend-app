package com.selimhorri.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.exception.wrapper.OrderNotFoundException;
import com.selimhorri.app.helper.OrderMappingHelper;
import com.selimhorri.app.metrics.BusinessMetricsService;
import com.selimhorri.app.repository.OrderRepository;
import com.selimhorri.app.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
	
	private final OrderRepository orderRepository;
	private final BusinessMetricsService businessMetricsService;
	
	@Override
	public List<OrderDto> findAll() {
		log.info("*** OrderDto List, service; fetch all orders *");
		return this.orderRepository.findAll()
				.stream()
					.map(OrderMappingHelper::map)
					.distinct()
					.collect(Collectors.toUnmodifiableList());
	}
	
	@Override
	public OrderDto findById(final Integer orderId) {
		log.info("*** OrderDto, service; fetch order by id *");
		return this.orderRepository.findById(orderId)
				.map(OrderMappingHelper::map)
				.orElseThrow(() -> new OrderNotFoundException(String
						.format("Order with id: %d not found", orderId)));
	}
	
	@Override
	public OrderDto save(final OrderDto orderDto) {
		log.info("*** OrderDto, service; save order *");
		OrderDto savedOrder = OrderMappingHelper.map(this.orderRepository
				.save(OrderMappingHelper.map(orderDto)));
		// Registrar métrica de negocio: orden creada
		businessMetricsService.recordOrderCreated(savedOrder.getOrderFee());
		return savedOrder;
	}
	
	@Override
	public OrderDto update(final OrderDto orderDto) {
		log.info("*** OrderDto, service; update order *");
		OrderDto updatedOrder = OrderMappingHelper.map(this.orderRepository
				.save(OrderMappingHelper.map(orderDto)));
		// Registrar métrica de negocio: orden actualizada
		businessMetricsService.recordOrderUpdated();
		return updatedOrder;
	}
	
	@Override
	public OrderDto update(final Integer orderId, final OrderDto orderDto) {
		log.info("*** OrderDto, service; update order with orderId *");
		OrderDto updatedOrder = OrderMappingHelper.map(this.orderRepository
				.save(OrderMappingHelper.map(this.findById(orderId))));
		// Registrar métrica de negocio: orden actualizada
		businessMetricsService.recordOrderUpdated();
		return updatedOrder;
	}
	
	@Override
	public void deleteById(final Integer orderId) {
		log.info("*** Void, service; delete order by id *");
		this.orderRepository.delete(OrderMappingHelper.map(this.findById(orderId)));
		// Registrar métrica de negocio: orden eliminada
		businessMetricsService.recordOrderDeleted();
	}
	
	
	
}










