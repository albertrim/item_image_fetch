package com.example.imagefetch.exception;

public class TimeoutException extends ImageFetchException {
    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}