package com.kkpa.scrumgraph.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.kkpa.scrumgraph.entities.User;

public interface UserRepository extends Neo4jRepository<User, Long> {

	
	
}
