package com.selimhorri.app.metrics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para registrar métricas de negocio del Product Service.
 * Métricas relacionadas con productos:
 * - Productos creados, actualizados, eliminados
 * - Total de productos en catálogo
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessMetricsService {
	
	private final MeterRegistry meterRegistry;
	
	// Counters para eventos de productos
	private Counter productsCreatedCounter;
	private Counter productsUpdatedCounter;
	private Counter productsDeletedCounter;
	
	/**
	 * Inicializa las métricas al crear el bean.
	 */
	private void initializeMetrics() {
		if (productsCreatedCounter == null) {
			productsCreatedCounter = Counter.builder("business.products.created")
					.description("Número total de productos creados")
					.tag("service", "product-service")
					.register(meterRegistry);
			
			productsUpdatedCounter = Counter.builder("business.products.updated")
					.description("Número total de productos actualizados")
					.tag("service", "product-service")
					.register(meterRegistry);
			
			productsDeletedCounter = Counter.builder("business.products.deleted")
					.description("Número total de productos eliminados")
					.tag("service", "product-service")
					.register(meterRegistry);
			
			log.info("Business metrics initialized for Product Service");
		}
	}
	
	/**
	 * Registra la creación de un nuevo producto.
	 */
	public void recordProductCreated() {
		initializeMetrics();
		productsCreatedCounter.increment();
		log.debug("Business metric recorded: product created");
	}
	
	/**
	 * Registra la actualización de un producto.
	 */
	public void recordProductUpdated() {
		initializeMetrics();
		productsUpdatedCounter.increment();
		log.debug("Business metric recorded: product updated");
	}
	
	/**
	 * Registra la eliminación de un producto.
	 */
	public void recordProductDeleted() {
		initializeMetrics();
		productsDeletedCounter.increment();
		log.debug("Business metric recorded: product deleted");
	}
	
	/**
	 * Actualiza el gauge de total de productos en catálogo.
	 * Este método debe ser llamado periódicamente o cuando cambie el número de productos.
	 * 
	 * @param totalProducts Número actual de productos en catálogo
	 */
	public void updateTotalProductsGauge(long totalProducts) {
		Gauge.builder("business.products.total", () -> totalProducts)
				.description("Número total de productos en el catálogo")
				.tag("service", "product-service")
				.register(meterRegistry);
		log.debug("Business metric updated: total products = {}", totalProducts);
	}
	
}

