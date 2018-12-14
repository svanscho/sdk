package com.openmotics.exceptions;

/**
 * Created by svanscho on 11/12/2018.
 */

//This Exception is raised when the user credentials are not valid.
public class AuthenticationException extends ApiException {
    public AuthenticationException() {
        super("Authentication error: the provided credentials are not valid.");
    }
    public AuthenticationException(String message) {
        super(String.format("Authentication error: %s", message));
    }
}
