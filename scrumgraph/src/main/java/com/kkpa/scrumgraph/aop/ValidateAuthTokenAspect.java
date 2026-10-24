package com.kkpa.scrumgraph.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.kkpa.scrumgraph.constants.ECodeResponse;
import com.kkpa.scrumgraph.dto.ScrumHeaderDTO;
import com.kkpa.scrumgraph.exceptionhandler.SGException;
import com.kkpa.scrumgraph.services.UserService;

/**
 * Checks the auth token before a method marked with {@link ValidateAuthToken} runs.
 * The token is the first String argument, or the token inside the first
 * ScrumHeaderDTO argument. With no token, the call is rejected.
 */
@Aspect
@Component
public class ValidateAuthTokenAspect {

	private final UserService userService;

	public ValidateAuthTokenAspect(UserService userService) {
		this.userService = userService;
	}

	@Around("@annotation(com.kkpa.scrumgraph.aop.ValidateAuthToken)")
	public Object validateAuthToken(ProceedingJoinPoint joinPoint) throws Throwable {
		String authToken = findAuthToken(joinPoint);

		if (!userService.validateToken(authToken)) {
			throw new SGException(ECodeResponse.UNAUTHENTICATED_USER);
		}

		return joinPoint.proceed();
	}

	private static String findAuthToken(ProceedingJoinPoint joinPoint) {
		Class<?>[] paramTypes = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
		Object[] args = joinPoint.getArgs();

		for (int i = 0; i < paramTypes.length; i++) {
			if (String.class.equals(paramTypes[i])) {
				return (String) args[i];
			}
			if (ScrumHeaderDTO.class.equals(paramTypes[i])) {
				ScrumHeaderDTO header = (ScrumHeaderDTO) args[i];
				return header == null ? null : header.getAuthToken();
			}
		}

		return null;
	}

}
