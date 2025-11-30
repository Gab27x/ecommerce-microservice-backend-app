package com.selimhorri.app.metrics;

import org.springframework.stereotype.Component;

import com.selimhorri.app.domain.PaymentStatus;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para registrar métricas de negocio del Payment Service.
 * Estas métricas miden aspectos relacionados con pagos:
 * - Pagos creados, exitosos, fallidos
 * - Pagos por estado
 * - Monto total procesado
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessMetricsService {
	
	private final MeterRegistry meterRegistry;
	
	// Counters para eventos de pagos
	private Counter paymentsCreatedCounter;
	private Counter paymentsSuccessfulCounter;
	private Counter paymentsFailedCounter;
	
	/**
	 * Inicializa las métricas al crear el bean.
	 */
	private void initializeMetrics() {
		if (paymentsCreatedCounter == null) {
			paymentsCreatedCounter = Counter.builder("business.payments.created")
					.description("Número total de pagos creados")
					.tag("service", "payment-service")
					.register(meterRegistry);
			
			paymentsSuccessfulCounter = Counter.builder("business.payments.successful")
					.description("Número total de pagos exitosos")
					.tag("service", "payment-service")
					.tag("result", "success")
					.register(meterRegistry);
			
			paymentsFailedCounter = Counter.builder("business.payments.failed")
					.description("Número total de pagos fallidos")
					.tag("service", "payment-service")
					.tag("result", "failed")
					.register(meterRegistry);
			
			log.info("Business metrics initialized for Payment Service");
		}
	}
	
	/**
	 * Registra la creación de un nuevo pago.
	 */
	public void recordPaymentCreated() {
		initializeMetrics();
		paymentsCreatedCounter.increment();
		log.debug("Business metric recorded: payment created");
	}
	
	/**
	 * Registra un pago exitoso.
	 * Un pago es exitoso cuando isPayed = true y status = COMPLETED
	 */
	public void recordPaymentSuccessful() {
		initializeMetrics();
		paymentsSuccessfulCounter.increment();
		log.debug("Business metric recorded: payment successful");
	}
	
	/**
	 * Registra un pago fallido.
	 * Un pago es fallido cuando isPayed = false o status != COMPLETED
	 */
	public void recordPaymentFailed() {
		initializeMetrics();
		paymentsFailedCounter.increment();
		log.debug("Business metric recorded: payment failed");
	}
	
	/**
	 * Registra un pago por su estado.
	 * 
	 * @param status Estado del pago (NOT_STARTED, IN_PROGRESS, COMPLETED)
	 */
	public void recordPaymentByStatus(PaymentStatus status) {
		initializeMetrics();
		meterRegistry.counter("business.payments.by.status",
				"service", "payment-service",
				"status", status != null ? status.name() : "UNKNOWN")
			.increment();
		log.debug("Business metric recorded: payment with status {}", status);
	}
	
	/**
	 * Registra un pago exitoso con su estado.
	 * 
	 * @param status Estado del pago
	 * @param isPayed Si el pago fue completado
	 */
	public void recordPayment(PaymentStatus status, Boolean isPayed) {
		recordPaymentByStatus(status);
		if (isPayed != null && isPayed && status == PaymentStatus.COMPLETED) {
			recordPaymentSuccessful();
		} else {
			recordPaymentFailed();
		}
	}
	
}

