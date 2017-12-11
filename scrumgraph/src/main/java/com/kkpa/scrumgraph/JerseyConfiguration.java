package com.kkpa.scrumgraph;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
@ApplicationPath("/scrumgraph/sgrest")
public class JerseyConfiguration extends ResourceConfig {
	
	public JerseyConfiguration() {
		setUp();
	}

	private void setUp() {
		packages("com.kkpa.scrumgraph.controllers");
	}

}
