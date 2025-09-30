package com.example.imagefetch.exception;

public class InvalidUrlException extends ImageFetchException {
    public InvalidUrlException(String message) {
        super(message);
    }

    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}