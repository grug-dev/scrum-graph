package com.kkpa.scrumgraph.exceptionhandler;

import org.neo4j.ogm.drivers.http.request.HttpRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kkpa.scrumgraph.constants.ECodeResponse;
import com.kkpa.scrumgraph.dto.ResponseDTO;

/**
 * Turns exceptions into a ResponseDTO body with a real HTTP status.
 */
@RestControllerAdvice
public class SGRestControllerAdvice {

	private static final Logger LOGGER = LoggerFactory.getLogger(SGRestControllerAdvice.class);

	@ExceptionHandler(SGException.class)
	public ResponseEntity<ResponseDTO> handleSGException(SGException ex) {
		return buildError(ex.getCodeResponse(), ex.getMessage());
	}

	@ExceptionHandler({ CannotCreateTransactionException.class, HttpRequestException.class })
	public ResponseEntity<ResponseDTO> handleDatabaseException(Exception ex) {
		LOGGER.error("Database connection failed", ex);
		return buildError(ECodeResponse.DB_CONNECTION_ERROR, ECodeResponse.DB_CONNECTION_ERROR.getStatus());
	}

	/** Missing header, or a header that is not valid JSON. */
	@ExceptionHandler({ ServletRequestBindingException.class, TypeMismatchException.class })
	public ResponseEntity<ResponseDTO> handleBadRequest(Exception ex) {
		return buildError(ECodeResponse.INVALID_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseDTO> handleException(Exception ex) {
		// A JDK proxy wraps a checked exception that the interface does not declare.
		if (ex.getCause() instanceof SGException) {
			return handleSGException((SGException) ex.getCause());
		}

		LOGGER.error("Unexpected error", ex);
		return buildError(ECodeResponse.ERROR_CODE, "Internal Error");
	}

	private ResponseEntity<ResponseDTO> buildError(ECodeResponse codeResponse, String errorMsg) {
		ResponseDTO respDTO = new ResponseDTO();
		respDTO.setErrorMsg(errorMsg);
		respDTO.setCode(codeResponse.getCode());
		respDTO.setStatus(ECodeResponse.ERROR_CODE.getStatus());

		return ResponseEntity.status(codeResponse.getHttpStatus()).body(respDTO);
	}

}
