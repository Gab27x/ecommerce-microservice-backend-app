package com.selimhorri.app.config.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfig {
	
	@LoadBalanced
	@Bean
	@RefreshScope
	public RestTemplate restTemplateBean() {
		return new RestTemplate();
	}
}










