package com.selimhorri.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Example of refreshable properties for product-service.
 * These properties come from the Config Server and can be refreshed without restarting the service.
 * 
 * Usage: Call POST /product-service/actuator/refresh to reload these properties
 * after updating them in Config Server.
 */
@Component
@RefreshScope
public class RefreshableProperties {
	
	/**
	 * Maximum number of products to return in a single query.
	 * Default: 50
	 */
	@Value("${app.product.max-results:50}")
	private int maxProductsPerQuery;
	
	/**
	 * Enable/disable product caching.
	 * Default: true
	 */
	@Value("${app.product.enable-cache:true}")
	private boolean enableProductCache;
	
	/**
	 * Cache expiration time in seconds.
	 * Default: 600 seconds (10 minutes)
	 */
	@Value("${app.product.cache-expiration:600}")
	private int cacheExpirationSeconds;
	
	/**
	 * Search timeout in milliseconds.
	 * Default: 3000ms (3 seconds)
	 */
	@Value("${app.product.search-timeout:3000}")
	private int searchTimeout;
	
	// Getters
	public int getMaxProductsPerQuery() {
		return maxProductsPerQuery;
	}
	
	public boolean isEnableProductCache() {
		return enableProductCache;
	}
	
	public int getCacheExpirationSeconds() {
		return cacheExpirationSeconds;
	}
	
	public int getSearchTimeout() {
		return searchTimeout;
	}
	
}

