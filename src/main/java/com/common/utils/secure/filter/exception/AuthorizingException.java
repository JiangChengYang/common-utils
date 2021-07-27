package com.common.utils.secure.filter.exception;

public class AuthorizingException extends Exception{
    public AuthorizingException(String message) {
        super(message);
    }

    public AuthorizingException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
