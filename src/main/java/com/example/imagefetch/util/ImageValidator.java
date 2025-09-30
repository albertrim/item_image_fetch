package com.example.imagefetch.util;

import com.example.imagefetch.exception.InvalidUrlException;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Component
public class ImageValidator {

    private static final List<String> ALLOWED_FORMATS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "webp"
    );

    public boolean isValidImageFormat(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        String lowerUrl = url.toLowerCase();
        return ALLOWED_FORMATS.stream()
            .anyMatch(format -> lowerUrl.endsWith("." + format) || lowerUrl.contains("." + format + "?"));
    }

    public void validateImageUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidUrlException("Image URL cannot be null or empty");
        }

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new InvalidUrlException("Invalid URL format: " + url, e);
        }

        if (!isValidImageFormat(url)) {
            throw new InvalidUrlException("Unsupported image format. Allowed formats: " + ALLOWED_FORMATS);
        }
    }
}