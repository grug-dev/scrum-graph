package com.kkpa.scrumgraph.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;

import com.kkpa.scrumgraph.constants.ECodeResponse;
import com.kkpa.scrumgraph.dto.ScrumHeaderDTO;
import com.kkpa.scrumgraph.exceptionhandler.SGException;
import com.kkpa.scrumgraph.services.UserService;

public class ValidateAuthTokenAspectTest {

	private static final String VALID_TOKEN = "valid-token";

	private UserService userService;

	private ProceedingJoinPoint joinPoint;

	private MethodSignature signature;

	private ValidateAuthTokenAspect aspect;

	@Before
	public void setUp() {
		userService = mock(UserService.class);
		joinPoint = mock(ProceedingJoinPoint.class);
		signature = mock(MethodSignature.class);
		when(joinPoint.getSignature()).thenReturn(signature);
		when(userService.validateToken(VALID_TOKEN)).thenReturn(true);

		aspect = new ValidateAuthTokenAspect(userService);
	}

	@Test
	public void validTokenRunsTheMethod() throws Throwable {
		givenArgs(new Class<?>[] { String.class }, new Object[] { VALID_TOKEN });
		Object result = new Object();
		when(joinPoint.proceed()).thenReturn(result);

		assertSame(result, aspect.validateAuthToken(joinPoint));
	}

	@Test
	public void validTokenInsideHeaderRunsTheMethod() throws Throwable {
		ScrumHeaderDTO header = new ScrumHeaderDTO();
		header.setAuthToken(VALID_TOKEN);
		givenArgs(new Class<?>[] { Long.class, ScrumHeaderDTO.class }, new Object[] { 1L, header });
		Object result = new Object();
		when(joinPoint.proceed()).thenReturn(result);

		assertSame(result, aspect.validateAuthToken(joinPoint));
	}

	@Test
	public void invalidTokenIsRejected() throws Throwable {
		givenArgs(new Class<?>[] { String.class }, new Object[] { "bad-token" });

		assertRejected();
	}

	@Test
	public void nullTokenIsRejected() throws Throwable {
		givenArgs(new Class<?>[] { String.class }, new Object[] { null });

		assertRejected();
	}

	@Test
	public void methodWithoutTokenArgumentIsRejected() throws Throwable {
		givenArgs(new Class<?>[] { Long.class }, new Object[] { 1L });

		assertRejected();
	}

	@Test
	public void exceptionFromTheMethodGoesThroughUnchanged() throws Throwable {
		givenArgs(new Class<?>[] { String.class }, new Object[] { VALID_TOKEN });
		IllegalStateException original = new IllegalStateException("boom");
		when(joinPoint.proceed()).thenThrow(original);

		try {
			aspect.validateAuthToken(joinPoint);
			fail("Expected IllegalStateException");
		} catch (IllegalStateException ex) {
			assertSame(original, ex);
		}
	}

	private void givenArgs(Class<?>[] types, Object[] args) {
		when(signature.getParameterTypes()).thenReturn(types);
		when(joinPoint.getArgs()).thenReturn(args);
	}

	private void assertRejected() throws Throwable {
		try {
			aspect.validateAuthToken(joinPoint);
			fail("Expected SGException");
		} catch (SGException ex) {
			assertEquals(ECodeResponse.UNAUTHENTICATED_USER, ex.getCodeResponse());
		}
		verify(joinPoint, never()).proceed();
	}

}
