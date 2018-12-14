package com.openmotics.exceptions;

/**
 * Created by svanscho on 11/12/2018.
 */

//This Exception is raised when a non successful message was returned.
public class ApiException extends Exception {
    public ApiException(String message) {
        super(String.format("API call failed: %s", message));
    }
}
