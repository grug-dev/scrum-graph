package com.kkpa.scrumgraph.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.kkpa.scrumgraph.auth.AuthTokenInterceptor;
import com.kkpa.scrumgraph.services.UserService;

/**
 * Registers the auth token check for all controllers.
 * Only methods marked with ValidateAuthToken are checked.
 */
@Configuration
public class AuthConfig extends WebMvcConfigurerAdapter {

	private final UserService userService;

	public AuthConfig(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new AuthTokenInterceptor(userService));
	}

}
