package com.example.imagefetch.exception;

public class ImageFetchException extends RuntimeException {
    public ImageFetchException(String message) {
        super(message);
    }

    public ImageFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}