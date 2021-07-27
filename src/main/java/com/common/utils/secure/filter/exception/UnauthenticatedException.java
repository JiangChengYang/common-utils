package com.common.utils.secure.filter.exception;

public class UnauthenticatedException extends AuthorizingException{
    public UnauthenticatedException(String message) {
        super(message);
    }

    public UnauthenticatedException(Throwable cause) {
        super(cause);
    }
}
