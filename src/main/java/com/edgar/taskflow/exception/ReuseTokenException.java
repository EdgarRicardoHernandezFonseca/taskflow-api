package com.edgar.taskflow.exception;

public class ReuseTokenException extends RuntimeException {
    public ReuseTokenException(String message) {
        super(message);
    }
}
