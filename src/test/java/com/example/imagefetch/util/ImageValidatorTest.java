package com.example.imagefetch.util;

import com.example.imagefetch.exception.InvalidUrlException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageValidatorTest {

    private ImageValidator imageValidator;

    @BeforeEach
    void setUp() {
        imageValidator = new ImageValidator();
    }

    @Test
    void testValidImageFormat_Jpg() {
        assertTrue(imageValidator.isValidImageFormat("https://example.com/image.jpg"));
    }

    @Test
    void testValidImageFormat_Png() {
        assertTrue(imageValidator.isValidImageFormat("https://example.com/image.png"));
    }

    @Test
    void testValidImageFormat_WithQueryParams() {
        assertTrue(imageValidator.isValidImageFormat("https://example.com/image.jpg?size=large"));
    }

    @Test
    void testInvalidImageFormat() {
        assertFalse(imageValidator.isValidImageFormat("https://example.com/document.pdf"));
    }

    @Test
    void testValidateImageUrl_Valid() {
        assertDoesNotThrow(() -> imageValidator.validateImageUrl("https://example.com/image.jpg"));
    }

    @Test
    void testValidateImageUrl_Null() {
        assertThrows(InvalidUrlException.class, () -> imageValidator.validateImageUrl(null));
    }

    @Test
    void testValidateImageUrl_InvalidFormat() {
        assertThrows(InvalidUrlException.class, () -> imageValidator.validateImageUrl("https://example.com/file.pdf"));
    }

    @Test
    void testValidateImageUrl_MalformedUrl() {
        assertThrows(InvalidUrlException.class, () -> imageValidator.validateImageUrl("not-a-url"));
    }
}