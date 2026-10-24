package com.kkpa.scrumgraph.exceptionhandler;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.UndeclaredThrowableException;

import org.junit.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.ServletRequestBindingException;

import com.kkpa.scrumgraph.constants.ECodeResponse;
import com.kkpa.scrumgraph.dto.ResponseDTO;

public class SGRestControllerAdviceTest {

	private final SGRestControllerAdvice advice = new SGRestControllerAdvice();

	@Test
	public void invalidTokenGives401() {
		ResponseEntity<ResponseDTO> response = advice
				.handleSGException(new SGException(ECodeResponse.UNAUTHENTICATED_USER));

		assertError(response, HttpStatus.UNAUTHORIZED, ECodeResponse.UNAUTHENTICATED_USER.getCode());
		assertEquals(ECodeResponse.UNAUTHENTICATED_USER.getStatus(), response.getBody().getErrorMsg());
	}

	@Test
	public void wrappedSGExceptionGivesItsOwnStatus() {
		Exception wrapped = new UndeclaredThrowableException(new SGException(ECodeResponse.UNAUTHENTICATED_USER));

		ResponseEntity<ResponseDTO> response = advice.handleException(wrapped);

		assertError(response, HttpStatus.UNAUTHORIZED, ECodeResponse.UNAUTHENTICATED_USER.getCode());
	}

	@Test
	public void databaseErrorGives503() {
		ResponseEntity<ResponseDTO> response = advice
				.handleDatabaseException(new CannotCreateTransactionException("no connection"));

		assertError(response, HttpStatus.SERVICE_UNAVAILABLE, ECodeResponse.DB_CONNECTION_ERROR.getCode());
	}

	@Test
	public void missingHeaderGives400() {
		ResponseEntity<ResponseDTO> response = advice
				.handleBadRequest(new ServletRequestBindingException("Missing header"));

		assertError(response, HttpStatus.BAD_REQUEST, ECodeResponse.INVALID_REQUEST.getCode());
	}

	@Test
	public void badHeaderValueGives400() {
		ResponseEntity<ResponseDTO> response = advice
				.handleBadRequest(new TypeMismatchException("{bad", Object.class));

		assertError(response, HttpStatus.BAD_REQUEST, ECodeResponse.INVALID_REQUEST.getCode());
	}

	@Test
	public void otherErrorGives500() {
		ResponseEntity<ResponseDTO> response = advice.handleException(new IllegalStateException("boom"));

		assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, ECodeResponse.ERROR_CODE.getCode());
		assertEquals("Internal Error", response.getBody().getErrorMsg());
	}

	private static void assertError(ResponseEntity<ResponseDTO> response, HttpStatus status, int code) {
		assertEquals(status, response.getStatusCode());
		assertEquals(code, response.getBody().getCode());
		assertEquals(ECodeResponse.ERROR_CODE.getStatus(), response.getBody().getStatus());
	}

}
