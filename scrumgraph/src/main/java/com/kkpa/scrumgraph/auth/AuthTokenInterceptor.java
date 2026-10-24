package com.kkpa.scrumgraph.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.kkpa.scrumgraph.constants.ECodeResponse;
import com.kkpa.scrumgraph.dto.ScrumHeaderDTO;
import com.kkpa.scrumgraph.exceptionhandler.SGException;
import com.kkpa.scrumgraph.services.UserService;

/**
 * Checks the auth token before a controller method marked with {@link ValidateAuthToken} runs.
 * The token always comes from the {@link ScrumHeaderDTO#HEADER_NAME} header,
 * never from the method arguments.
 */
public class AuthTokenInterceptor extends HandlerInterceptorAdapter {

	private final UserService userService;

	public AuthTokenInterceptor(UserService userService) {
		this.userService = userService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws SGException {
		if (!needsToken(handler)) {
			return true;
		}

		String authToken = readAuthToken(request.getHeader(ScrumHeaderDTO.HEADER_NAME));
		if (!userService.validateToken(authToken)) {
			throw new SGException(ECodeResponse.UNAUTHENTICATED_USER);
		}

		return true;
	}

	private static boolean needsToken(Object handler) {
		return handler instanceof HandlerMethod
				&& ((HandlerMethod) handler).hasMethodAnnotation(ValidateAuthToken.class);
	}

	/** A missing header, or a header that is not a JSON object, gives a 400. */
	private static String readAuthToken(String headerValue) throws SGException {
		try {
			return ScrumHeaderDTO.valueOf(headerValue).getAuthToken();
		} catch (IllegalArgumentException ex) {
			throw new SGException(ECodeResponse.INVALID_REQUEST, ex.getMessage());
		}
	}

}
