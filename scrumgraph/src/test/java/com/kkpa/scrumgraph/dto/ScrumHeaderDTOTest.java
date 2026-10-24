package com.kkpa.scrumgraph.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ScrumHeaderDTOTest {

	@Test
	public void readsTheAuthToken() {
		ScrumHeaderDTO header = ScrumHeaderDTO.valueOf("{\"authToken\": \"abc123\"}");

		assertEquals("abc123", header.getAuthToken());
	}

	@Test
	public void missingAuthTokenGivesNullToken() {
		assertNull(ScrumHeaderDTO.valueOf("{}").getAuthToken());
	}

	@Test
	public void authTokenThatIsNotTextGivesNullToken() {
		assertNull(ScrumHeaderDTO.valueOf("{\"authToken\": 123}").getAuthToken());
	}

	@Test(expected = IllegalArgumentException.class)
	public void badJsonIsRejected() {
		ScrumHeaderDTO.valueOf("{not json");
	}

	@Test(expected = IllegalArgumentException.class)
	public void jsonThatIsNotAnObjectIsRejected() {
		ScrumHeaderDTO.valueOf("[\"abc123\"]");
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyHeaderIsRejected() {
		ScrumHeaderDTO.valueOf("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullHeaderIsRejected() {
		ScrumHeaderDTO.valueOf(null);
	}

}
