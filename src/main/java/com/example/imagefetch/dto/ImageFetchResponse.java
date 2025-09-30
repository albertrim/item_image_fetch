package com.example.imagefetch.dto;

import java.util.List;

public record ImageFetchResponse(
    long totalLoadingTimeMs,
    List<ImageResult> images
) {
}