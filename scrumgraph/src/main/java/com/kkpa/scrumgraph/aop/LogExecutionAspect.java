package com.kkpa.scrumgraph.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogExecutionAspect {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LogExecutionAspect.class);
	
	@Around("@annotation(com.kkpa.scrumgraph.aop.LogExecutionTime)")
	public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.currentTimeMillis();
		
		// Do not log the arguments: they can hold the auth token.
		LOGGER.info("Start {}", joinPoint.getSignature().toShortString());
	    
		// Execute the method intercepted
		Object proceed = joinPoint.proceed();
	 
	    long executionTime = System.currentTimeMillis() - start;
	 
	    LOGGER.info("End {} executed in {} ms", joinPoint.getSignature().toShortString(), executionTime);
	    
	    return proceed;
	}

}
