package com.kkpa.scrumgraph.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.kkpa.scrumgraph.constants.ECodeResponse;
import com.kkpa.scrumgraph.exceptionhandler.SGException;
import com.kkpa.scrumgraph.services.UserService;

@Aspect
@Component
public class ValidateAuthTokenAspect {

	@Autowired
	@Qualifier("userSrv")
	private UserService usrService;
	
	@Around("@annotation(ValidateAuthToken)")
	public Object validateAuthToken(ProceedingJoinPoint joinPoint) throws SGException {
		String authToken = null;
		
		authToken = joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0].toString() : null;
		
		if (!usrService.validateToken(authToken) ){
	    	throw new SGException(ECodeResponse.UNAUTHENTICATED_USER);
	    }
	    
		// Execute the method intercepted
		Object proceed = null;
		try {
			proceed = joinPoint.proceed();
		} catch (Throwable e) {
			throw new SGException(e.getMessage());
		}
	 
	    
	    return proceed;
	}
	
	
}
