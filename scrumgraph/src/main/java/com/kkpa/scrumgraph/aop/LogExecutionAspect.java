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
	
	@Around("@annotation(LogExecutionTime)")
	public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.currentTimeMillis();
		
		LOGGER.info("INICIO " + joinPoint.getSignature().getDeclaringTypeName() +  " - " + joinPoint.getSignature().getName() + " - " + joinPoint.getArgs()); 
	    
		// Execute the method intercepted
		Object proceed = joinPoint.proceed();
	 
	    long executionTime = System.currentTimeMillis() - start;
	 
	    LOGGER.info("FIN " + joinPoint.getSignature() + " executed in " + executionTime + "ms");
	    
	    return proceed;
	}

}
