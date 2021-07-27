package com.common.utils.secure.filter.exception;

public class LacksPermissionException extends AuthorizingException{
    public LacksPermissionException(String message) {
        super(message);
    }
}
