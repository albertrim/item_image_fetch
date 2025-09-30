package com.example.imagefetch.strategy;

import com.example.imagefetch.dto.ImageFetchRequest;
import com.example.imagefetch.dto.ImageResult;
import com.example.imagefetch.dto.ImageSource;
import com.example.imagefetch.exception.TimeoutException;
import com.example.imagefetch.service.PerformanceMetricsService;
import com.example.imagefetch.util.ImageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectUrlImageFetchStrategy implements ImageFetchStrategy {

    private final WebClient webClient;
    private final ImageValidator imageValidator;
    private final PerformanceMetricsService performanceMetricsService;

    @Value("${image-fetch.strategy.direct-url.timeout:500}")
    private int timeoutMs;

    @Override
    public boolean canHandle(ImageFetchRequest request) {
        return request.imageUrl() != null && !request.imageUrl().isBlank();
    }

    @Override
    public List<ImageResult> fetchImages(ImageFetchRequest request) {
        if (!canHandle(request)) {
            return Collections.emptyList();
        }

        String imageUrl = request.imageUrl();
        log.debug("Fetching image from direct URL: {}", imageUrl);

        try {
            imageValidator.validateImageUrl(imageUrl);

            long startTime = System.currentTimeMillis();

            byte[] imageBytes = webClient.get()
                .uri(imageUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();

            long loadingTime = System.currentTimeMillis() - startTime;

            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("Empty image data from URL: {}", imageUrl);
                return Collections.emptyList();
            }

            String resolution = performanceMetricsService.getImageResolution(imageBytes);
            long fileSize = imageBytes.length;

            ImageResult result = new ImageResult(
                imageUrl,
                ImageSource.DIRECT,
                loadingTime,
                resolution,
                fileSize
            );

            log.info("Successfully fetched direct URL image in {}ms, size: {} bytes", loadingTime, fileSize);
            return List.of(result);

        } catch (java.util.concurrent.TimeoutException e) {
            log.error("Timeout fetching image from direct URL: {}", imageUrl);
            throw new TimeoutException("Timeout fetching image from direct URL", e);
        } catch (Exception e) {
            log.error("Error fetching image from direct URL: {}", imageUrl, e);
            return Collections.emptyList();
        }
    }

    @Override
    public int getPriority() {
        return 1;
    }
}