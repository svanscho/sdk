package com.openmotics.exceptions;

/**
 * Created by svanscho on 11/12/2018.
 */
//This Exception is raised when the user credentials are not valid.
public class ForbiddenException extends ApiException {
    public ForbiddenException() {
        super("You are not allowed to perform actions on this resource.");
    }
}