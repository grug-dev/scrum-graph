package com.kkpa.scrumgraph.constants;

import org.springframework.http.HttpStatus;

/**
 * Application response codes.
 * Each code also names the HTTP status that the API returns with it.
 */
public enum ECodeResponse {

	OK(200, "OK", HttpStatus.OK),
	ERROR_CODE(500, "error", HttpStatus.INTERNAL_SERVER_ERROR),
	DB_CONNECTION_ERROR(503, "Database connection failed", HttpStatus.SERVICE_UNAVAILABLE),
	INVALID_REQUEST(101, "Invalid Request", HttpStatus.BAD_REQUEST),
	INCOMPLETE_REQUEST_FIELDS(202, "Incomplete request fields", HttpStatus.BAD_REQUEST),
	INVALID_FIELDS(203, "Invalid Fields", HttpStatus.BAD_REQUEST),
	INVALID_REQ_PAYLOAD(204, "Invalid Request PayLoad", HttpStatus.BAD_REQUEST),

	// USER CODES
	INVALID_EMAIL(310, "Invalid Email", HttpStatus.BAD_REQUEST),
	UNAUTHENTICATED_USER(311, "Unauthenticated user", HttpStatus.UNAUTHORIZED),
	UNAUTHORIZED_USER(312, "Unauthorized user", HttpStatus.FORBIDDEN),
	EMAIL_ALREADY_EXISTS(313, "Email already exists", HttpStatus.CONFLICT),
	USER_DOESNT_EXISTS(314, "User doesn't exists", HttpStatus.NOT_FOUND),
	USER_UNAVAILABLE(315, "User unavailable", HttpStatus.FORBIDDEN),

	;

	private final int code;

	private final String status;

	private final HttpStatus httpStatus;

	private ECodeResponse(int pCode, String pStatus, HttpStatus pHttpStatus) {
		this.code = pCode;
		this.status = pStatus;
		this.httpStatus = pHttpStatus;
	}

	public int getCode() {
		return code;
	}

	public String getStatus() {
		return status;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

}
