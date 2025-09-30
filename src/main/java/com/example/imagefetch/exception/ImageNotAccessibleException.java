package com.example.imagefetch.exception;

/**
 * Exception thrown when an image URL is not accessible
 */
public class ImageNotAccessibleException extends ImageFetchException {

    public ImageNotAccessibleException(String message) {
        super(message);
    }

    public ImageNotAccessibleException(String message, Throwable cause) {
        super(message, cause);
    }
}