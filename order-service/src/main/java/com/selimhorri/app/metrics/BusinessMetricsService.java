package com.selimhorri.app.metrics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para registrar métricas de negocio del Order Service.
 * Estas métricas miden aspectos relacionados con el dominio del negocio:
 * - Órdenes creadas, eliminadas
 * - Valor total y promedio de órdenes
 * - Conversión de carrito a orden
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessMetricsService {
	
	private final MeterRegistry meterRegistry;
	
	// Counters para eventos de órdenes
	private Counter ordersCreatedCounter;
	private Counter ordersDeletedCounter;
	private Counter ordersUpdatedCounter;
	
	// Summary para valores de órdenes
	private DistributionSummary orderValueSummary;
	
	/**
	 * Inicializa las métricas al crear el bean.
	 * Usa lazy initialization para evitar problemas de orden de inicialización.
	 */
	private void initializeMetrics() {
		if (ordersCreatedCounter == null) {
			ordersCreatedCounter = Counter.builder("business.orders.created")
					.description("Número total de órdenes creadas")
					.tag("service", "order-service")
					.register(meterRegistry);
			
			ordersDeletedCounter = Counter.builder("business.orders.deleted")
					.description("Número total de órdenes eliminadas")
					.tag("service", "order-service")
					.register(meterRegistry);
			
			ordersUpdatedCounter = Counter.builder("business.orders.updated")
					.description("Número total de órdenes actualizadas")
					.tag("service", "order-service")
					.register(meterRegistry);
			
			orderValueSummary = DistributionSummary.builder("business.orders.value")
					.description("Valor monetario de las órdenes")
					.baseUnit("currency")
					.tag("service", "order-service")
					.register(meterRegistry);
			
			log.info("Business metrics initialized for Order Service");
		}
	}
	
	/**
	 * Registra la creación de una nueva orden.
	 * 
	 * @param orderFee Valor de la orden
	 */
	public void recordOrderCreated(Double orderFee) {
		initializeMetrics();
		ordersCreatedCounter.increment();
		if (orderFee != null && orderFee > 0) {
			orderValueSummary.record(orderFee);
		}
		log.debug("Business metric recorded: order created with value {}", orderFee);
	}
	
	/**
	 * Registra la actualización de una orden.
	 */
	public void recordOrderUpdated() {
		initializeMetrics();
		ordersUpdatedCounter.increment();
		log.debug("Business metric recorded: order updated");
	}
	
	/**
	 * Registra la eliminación de una orden.
	 */
	public void recordOrderDeleted() {
		initializeMetrics();
		ordersDeletedCounter.increment();
		log.debug("Business metric recorded: order deleted");
	}
	
	/**
	 * Registra el valor de una orden (útil para actualizar métricas de valor total).
	 * 
	 * @param orderFee Valor de la orden
	 */
	public void recordOrderValue(Double orderFee) {
		initializeMetrics();
		if (orderFee != null && orderFee > 0) {
			orderValueSummary.record(orderFee);
		}
	}
	
}

