package com.kkpa.scrumgraph.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.kkpa.scrumgraph.dto.ScrumHeaderDTO;

/**
 * One CORS setup for the whole API.
 * Change it here when the allowed origins, methods or headers change.
 */
@Configuration
public class CorsConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("*")
				.allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS")
				.allowedHeaders("X-Requested-With", "Content-Type", ScrumHeaderDTO.HEADER_NAME)
				.allowCredentials(false);
	}

}
