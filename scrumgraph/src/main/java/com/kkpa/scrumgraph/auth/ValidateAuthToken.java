package com.kkpa.scrumgraph.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method that needs a valid auth token.
 * {@link AuthTokenInterceptor} checks the token before the method runs.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateAuthToken {

}
