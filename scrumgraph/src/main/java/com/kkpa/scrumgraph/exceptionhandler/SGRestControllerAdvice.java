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
public class SGRestControllerAdvice {

	@Autowired
	private ApplicationContext appCtx;
	
	
	@ExceptionHandler( { CannotCreateTransactionException.class , HttpRequestException.class })
	public ResponseDTO handleRequestException() {
		ResponseDTO respDTO = (ResponseDTO) appCtx.getBean("responseDTO");
		
		respDTO.setErrorMsg("La conexión a la base de datos ha fallado");
		respDTO.setCode(ECodeResponse.ERROR_CODE.getCode());
		respDTO.setStatus(ECodeResponse.ERROR_CODE.getStatus());
		
		return respDTO;
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseDTO handleRequestException(Exception ex) {
		ResponseDTO respDTO = (ResponseDTO) appCtx.getBean("responseDTO");
		
		if (ex.getCause() instanceof SGException) { 
			SGException sgException = (SGException) ex.getCause();
			respDTO.setErrorMsg(sgException.getMessage());
			respDTO.setCode(sgException.getCode());
			respDTO.setStatus(ECodeResponse.ERROR_CODE.getStatus());
			
			return respDTO;
		}
		
		respDTO.setErrorMsg("Internal Error");
		respDTO.setCode(ECodeResponse.ERROR_CODE.getCode());
		respDTO.setStatus(ECodeResponse.ERROR_CODE.getStatus());
		
		return respDTO;
	}
	
	
}
