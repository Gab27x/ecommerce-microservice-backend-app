package com.selimhorri.app.config;

import feign.Request;
import feign.Retryer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Feign Clients with Resilience4j Retry pattern.
 * This configures Feign to work with Resilience4j Retry and provides
 * refreshable timeout configurations.
 * 
 * Resilience4j Retry is configured via application.yml and automatically
 * applies to all Feign Clients when spring-cloud-starter-circuitbreaker-resilience4j
 * is on the classpath.
 */
@Configuration
public class FeignClientConfig {
	
	/**
	 * Configure Feign Request Options with refreshable timeout settings.
	 * These values come from RefreshableProperties and can be updated
	 * via Configuration Refresh pattern without restarting the service.
	 */
	@Bean
	@RefreshScope
	public Request.Options requestOptions(RefreshableProperties props) {
		// Use refreshable properties for timeouts
		// These values will be refreshed when Configuration Refresh is triggered
		long connectTimeout = Math.min(props.getApiTimeout() / 2, 5000);
		long readTimeout = props.getApiTimeout();
		
		return new Request.Options(
			(int) connectTimeout, TimeUnit.MILLISECONDS,
			(int) readTimeout, TimeUnit.MILLISECONDS,
			true // follow redirects
		);
	}
	
	/**
	 * Configure Feign Retryer as a fallback.
	 * Resilience4j Retry (configured in application.yml) is the primary
	 * retry mechanism with exponential backoff and configurable exceptions.
	 * This provides basic retry at the Feign level as a fallback.
	 * 
	 * Note: Resilience4j Retry will be applied automatically to all Feign Clients
	 * when spring-cloud-starter-circuitbreaker-resilience4j is on the classpath.
	 */
	@Bean
	@RefreshScope
	public Retryer feignRetryer(RefreshableProperties props) {
		// Retry configuration using refreshable properties:
		// - maxAttempts: based on refreshable properties
		// - period: based on refreshable retry delay
		// - maxPeriod: 3 seconds (maximum wait time)
		return new Retryer.Default(
			props.getRetryDelay(), // initial interval from refreshable properties
			3000, // max interval
			props.getMaxRetries() // max attempts from refreshable properties
		);
	}
	
}

