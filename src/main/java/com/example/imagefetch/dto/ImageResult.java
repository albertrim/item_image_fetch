package com.example.imagefetch.dto;

public record ImageResult(
    String url,
    ImageSource source,
    long loadingTimeMs,
    String resolution,
    long fileSizeBytes
) {
}