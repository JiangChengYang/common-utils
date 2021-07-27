package com.common.utils.excel;

public class InvalidValueException extends Exception{
    public InvalidValueException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
