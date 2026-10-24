package com.kkpa.scrumgraph.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import com.kkpa.scrumgraph.constants.ECodeResponse;
import com.kkpa.scrumgraph.dto.ScrumHeaderDTO;
import com.kkpa.scrumgraph.exceptionhandler.SGException;
import com.kkpa.scrumgraph.services.UserService;

public class AuthTokenInterceptorTest {

	private static final String VALID_TOKEN = "valid-token";

	private UserService userService;

	private AuthTokenInterceptor interceptor;

	private MockHttpServletRequest request;

	private final MockHttpServletResponse response = new MockHttpServletResponse();

	@Before
	public void setUp() {
		userService = mock(UserService.class);
		when(userService.validateToken(VALID_TOKEN)).thenReturn(true);

		interceptor = new AuthTokenInterceptor(userService);
		request = new MockHttpServletRequest();
	}

	@Test
	public void validTokenLetsTheRequestGo() throws Exception {
		givenHeader("{\"authToken\": \"" + VALID_TOKEN + "\"}");

		assertTrue(interceptor.preHandle(request, response, handler("secured")));
	}

	@Test
	public void invalidTokenGivesUnauthenticated() throws Exception {
		givenHeader("{\"authToken\": \"bad-token\"}");

		assertRejectedWith(ECodeResponse.UNAUTHENTICATED_USER);
	}

	@Test
	public void headerWithoutTokenGivesUnauthenticated() throws Exception {
		givenHeader("{}");

		assertRejectedWith(ECodeResponse.UNAUTHENTICATED_USER);
	}

	@Test
	public void missingHeaderGivesInvalidRequest() throws Exception {
		assertRejectedWith(ECodeResponse.INVALID_REQUEST);
		verify(userService, never()).validateToken(anyString());
	}

	@Test
	public void headerThatIsNotJsonGivesInvalidRequest() throws Exception {
		givenHeader("{not json");

		assertRejectedWith(ECodeResponse.INVALID_REQUEST);
	}

	@Test
	public void methodWithoutAnnotationIsNotChecked() throws Exception {
		assertTrue(interceptor.preHandle(request, response, handler("open")));
		verify(userService, never()).validateToken(anyString());
	}

	@Test
	public void handlerThatIsNotAMethodIsNotChecked() throws Exception {
		assertTrue(interceptor.preHandle(request, response, new Object()));
	}

	private void givenHeader(String value) {
		request.addHeader(ScrumHeaderDTO.HEADER_NAME, value);
	}

	private void assertRejectedWith(ECodeResponse expected) throws Exception {
		try {
			interceptor.preHandle(request, response, handler("secured"));
			fail("Expected SGException");
		} catch (SGException ex) {
			assertEquals(expected, ex.getCodeResponse());
		}
	}

	private static HandlerMethod handler(String methodName) throws NoSuchMethodException {
		return new HandlerMethod(new SampleController(), SampleController.class.getMethod(methodName));
	}

	static class SampleController {

		@ValidateAuthToken
		public void secured() {
		}

		public void open() {
		}

	}

}
