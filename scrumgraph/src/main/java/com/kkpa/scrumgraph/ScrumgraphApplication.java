package com.kkpa.scrumgraph;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories("com.kkpa.scrumgraph.repository")
@EntityScan("com.kkpa.scrumgraph.entities")
public class ScrumgraphApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScrumgraphApplication.class, args);
	}
}
