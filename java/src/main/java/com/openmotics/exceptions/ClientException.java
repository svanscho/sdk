package com.openmotics.exceptions;

/**
 * Created by svanscho on 11/12/2018.
 */

//This Exception is raised when the gateway is currently in maintenance mode.
public class ClientException extends Exception{
    public ClientException(String message) {
        super(String.format("Client SDK error: %s", message));
    }
}
