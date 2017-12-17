package com.kkpa.scrumgraph.exceptionhandler;

import org.neo4j.ogm.drivers.http.request.HttpRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kkpa.scrumgraph.constants.ECodeResponse;
import com.kkpa.scrumgraph.dto.ResponseDTO;

@RestControllerAdvice
public class RestControllerAdviceSG {

	@Autowired
	private ApplicationContext appCtx;
	
	@ExceptionHandler(SGException.class)
	public ResponseDTO handleRequestException(SGException ex) {
		ResponseDTO respDTO = (ResponseDTO) appCtx.getBean("responseDTO");
		
		respDTO.setErrorMsg(ex.getMessage());
		respDTO.setCode(ECodeResponse.ERROR_CODE.getCode());
		respDTO.setStatus(ECodeResponse.ERROR_CODE.getStatus());
		
		return respDTO;
	}
	
	@ExceptionHandler( { CannotCreateTransactionException.class , HttpRequestException.class })
	public ResponseDTO handleRequestException() {
		ResponseDTO respDTO = (ResponseDTO) appCtx.getBean("responseDTO");
		
		respDTO.setErrorMsg("La conexión ha fallado Catched");
		respDTO.setCode(404);
		respDTO.setStatus("ERROR");
		
		return respDTO;
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseDTO handleRequestException(Exception reqEx) {
		ResponseDTO respDTO = (ResponseDTO) appCtx.getBean("responseDTO");
		
		respDTO.setErrorMsg("Ha ocurrido un error inesperado. Por favor intenta más tarde mientras solucionamos el problema");
		respDTO.setCode(ECodeResponse.ERROR_CODE.getCode());
		respDTO.setStatus(ECodeResponse.ERROR_CODE.getStatus());
		
		return respDTO;
	}
	
	
	
}
