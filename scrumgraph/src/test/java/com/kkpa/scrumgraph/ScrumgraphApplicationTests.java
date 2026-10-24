package com.kkpa.scrumgraph;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Starts the full application. It needs a running Neo4j database,
 * so it is skipped by default (and in CI).
 * Run it with: mvn test -Dscrumgraph.it=true
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@IfProfileValue(name = "scrumgraph.it", value = "true")
public class ScrumgraphApplicationTests {

	@Test
	public void contextLoads() {
	}

}
