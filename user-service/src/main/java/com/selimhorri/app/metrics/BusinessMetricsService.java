package com.selimhorri.app.metrics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para registrar métricas de negocio del User Service.
 * Métricas relacionadas con usuarios:
 * - Usuarios registrados
 * - Usuarios activos
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessMetricsService {
	
	private final MeterRegistry meterRegistry;
	
	// Counter para usuarios registrados
	private Counter usersRegisteredCounter;
	private Counter usersUpdatedCounter;
	private Counter usersDeletedCounter;
	
	/**
	 * Inicializa las métricas al crear el bean.
	 */
	private void initializeMetrics() {
		if (usersRegisteredCounter == null) {
			usersRegisteredCounter = Counter.builder("business.users.registered")
					.description("Número total de usuarios registrados")
					.tag("service", "user-service")
					.register(meterRegistry);
			
			usersUpdatedCounter = Counter.builder("business.users.updated")
					.description("Número total de usuarios actualizados")
					.tag("service", "user-service")
					.register(meterRegistry);
			
			usersDeletedCounter = Counter.builder("business.users.deleted")
					.description("Número total de usuarios eliminados")
					.tag("service", "user-service")
					.register(meterRegistry);
			
			log.info("Business metrics initialized for User Service");
		}
	}
	
	/**
	 * Registra un nuevo usuario registrado.
	 */
	public void recordUserRegistered() {
		initializeMetrics();
		usersRegisteredCounter.increment();
		log.debug("Business metric recorded: user registered");
	}
	
	/**
	 * Registra la actualización de un usuario.
	 */
	public void recordUserUpdated() {
		initializeMetrics();
		usersUpdatedCounter.increment();
		log.debug("Business metric recorded: user updated");
	}
	
	/**
	 * Registra la eliminación de un usuario.
	 */
	public void recordUserDeleted() {
		initializeMetrics();
		usersDeletedCounter.increment();
		log.debug("Business metric recorded: user deleted");
	}
	
	/**
	 * Actualiza el gauge de usuarios activos.
	 * Este método debe ser llamado periódicamente o cuando cambie el número de usuarios activos.
	 * 
	 * @param activeUsersCount Número actual de usuarios activos
	 */
	public void updateActiveUsersGauge(long activeUsersCount) {
		Gauge.builder("business.users.active", () -> activeUsersCount)
				.description("Número actual de usuarios activos")
				.tag("service", "user-service")
				.register(meterRegistry);
		log.debug("Business metric updated: active users = {}", activeUsersCount);
	}
	
}

