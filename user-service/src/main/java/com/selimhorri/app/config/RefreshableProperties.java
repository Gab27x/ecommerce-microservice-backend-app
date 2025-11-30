package com.selimhorri.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;


@Component
@RefreshScope
public class RefreshableProperties {
	

	@Value("${app.database.connection-timeout:5000}")
	private int databaseConnectionTimeout;
	

	@Value("${app.user.max-results:100}")
	private int maxUsersPerQuery;
	

	@Value("${app.user.enable-cache:true}")
	private boolean enableUserCache;
	

	@Value("${app.user.cache-expiration:300}")
	private int cacheExpirationSeconds;

	public int getDatabaseConnectionTimeout() {
		return databaseConnectionTimeout;
	}
	
	public int getMaxUsersPerQuery() {
		return maxUsersPerQuery;
	}
	
	public boolean isEnableUserCache() {
		return enableUserCache;
	}
	
	public int getCacheExpirationSeconds() {
		return cacheExpirationSeconds;
	}
	
}

