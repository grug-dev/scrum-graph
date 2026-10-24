package com.kkpa.scrumgraph.dto;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ScrumHeaderDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Name of the HTTP header that holds this JSON. */
	public static final String HEADER_NAME = "X-ScrumGraph-Header";

	/** ObjectMapper is thread safe once it is set up, so one instance is shared. */
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private String authToken;

	/**
	 * Transform the JSON of the HTTP header into a ScrumHeaderDTO.
	 * AuthTokenInterceptor calls this method to read the header value.
	 * A missing or non text "authToken" gives a null token.
	 * @param sgHeader JSON text of the HTTP header
	 * @return ScrumHeaderDTO with the header's info, never null
	 * @throws IllegalArgumentException when the header is not a JSON object
	 */
	public static ScrumHeaderDTO valueOf(String sgHeader) {
		if (sgHeader == null) {
			throw new IllegalArgumentException(HEADER_NAME + " is missing");
		}

		JsonNode root;
		try {
			root = MAPPER.readTree(sgHeader);
		} catch (IOException e) {
			throw new IllegalArgumentException(HEADER_NAME + " is not valid JSON", e);
		}

		if (root == null || !root.isObject()) {
			throw new IllegalArgumentException(HEADER_NAME + " must be a JSON object");
		}

		JsonNode token = root.get("authToken");
		ScrumHeaderDTO shDTO = new ScrumHeaderDTO();
		shDTO.setAuthToken(token != null && token.isTextual() ? token.textValue() : null);

		return shDTO;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

}
