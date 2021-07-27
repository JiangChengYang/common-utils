package com.common.utils.secure.filter.exception;

public class NotExposedException extends AuthorizingException{
    public NotExposedException(String message) {
        super(message);
    }
}
