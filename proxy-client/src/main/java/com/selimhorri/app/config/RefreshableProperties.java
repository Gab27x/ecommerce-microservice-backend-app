package com.selimhorri.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Example of refreshable properties that can be updated via Configuration Refresh pattern.
 * These properties come from the Config Server and can be refreshed without restarting the service.
 * 
 * Usage: Call POST /actuator/refresh to reload these properties after updating them in Config Server.
 */
@Component
@RefreshScope
public class RefreshableProperties {
	
	/**
	 * API timeout configuration (in milliseconds).
	 * Default: 10000ms (10 seconds)
	 * This property can be updated in the Config Server and refreshed without restart.
	 */
	@Value("${app.api.timeout:10000}")
	private int apiTimeout;
	
	/**
	 * Maximum number of retries for external service calls.
	 * Default: 3
	 * This property can be updated in the Config Server and refreshed without restart.
	 */
	@Value("${app.api.max-retries:3}")
	private int maxRetries;
	
	/**
	 * Retry delay in milliseconds.
	 * Default: 1000ms (1 second)
	 * This property can be updated in the Config Server and refreshed without restart.
	 */
	@Value("${app.api.retry-delay:1000}")
	private long retryDelay;
	
	/**
	 * Feature flag for enabling/disabling certain features.
	 * Default: true
	 * This property can be updated in the Config Server and refreshed without restart.
	 */
	@Value("${app.features.enable-cache:true}")
	private boolean enableCache;
	
	/**
	 * Feature flag for enabling/disabling detailed logging.
	 * Default: false
	 * This property can be updated in the Config Server and refreshed without restart.
	 */
	@Value("${app.features.enable-detailed-logging:false}")
	private boolean enableDetailedLogging;
	
	// Getters
	public int getApiTimeout() {
		return apiTimeout;
	}
	
	public int getMaxRetries() {
		return maxRetries;
	}
	
	public long getRetryDelay() {
		return retryDelay;
	}
	
	public boolean isEnableCache() {
		return enableCache;
	}
	
	public boolean isEnableDetailedLogging() {
		return enableDetailedLogging;
	}
	
}

