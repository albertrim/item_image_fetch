package com.example.imagefetch.strategy;

import com.example.imagefetch.dto.ImageFetchRequest;
import com.example.imagefetch.dto.ImageResult;
import com.example.imagefetch.dto.ImageSource;
import com.example.imagefetch.exception.ImageNotAccessibleException;
import com.example.imagefetch.exception.InvalidUrlException;
import com.example.imagefetch.service.PerformanceMetricsService;
import com.example.imagefetch.util.HtmlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SalesUrlImageFetchStrategy implements ImageFetchStrategy {

    private final WebClient webClient;
    private final HtmlParser htmlParser;
    private final PerformanceMetricsService performanceMetricsService;

    @Value("${image-fetch.strategy.sales-url.timeout:200}")
    private int timeoutMs;

    @Value("${image-fetch.max-results:3}")
    private int maxResults;

    @Override
    public boolean canHandle(ImageFetchRequest request) {
        return request.salesUrl() != null && !request.salesUrl().isBlank();
    }

    @Override
    public List<ImageResult> fetchImages(ImageFetchRequest request) {
        if (!canHandle(request)) {
            return Collections.emptyList();
        }

        String salesUrl = request.salesUrl();
        log.debug("Fetching images from sales URL: {}", salesUrl);

        try {
            validateSalesUrl(salesUrl);

            long startTime = System.currentTimeMillis();

            // Fetch HTML content
            String html = webClient.get()
                .uri(salesUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();

            if (html == null || html.isBlank()) {
                log.warn("Empty HTML content from sales URL: {}", salesUrl);
                return Collections.emptyList();
            }

            // Extract representative images from HTML
            List<String> imageUrls = htmlParser.selectRepresentativeImages(html, maxResults);

            if (imageUrls.isEmpty()) {
                log.warn("No images found in sales URL: {}", salesUrl);
                return Collections.emptyList();
            }

            // Fetch metadata for each image
            List<ImageResult> results = new ArrayList<>();
            for (String imageUrl : imageUrls) {
                try {
                    ImageResult result = fetchImageMetadata(imageUrl, startTime);
                    results.add(result);
                } catch (Exception e) {
                    log.warn("Failed to fetch metadata for image: {}", imageUrl, e);
                    // Continue with other images
                }
            }

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("Successfully fetched {} images from sales URL in {}ms", results.size(), totalTime);
            return results;

        } catch (InvalidUrlException | ImageNotAccessibleException e) {
            // Re-throw custom exceptions for proper error handling
            throw e;
        } catch (WebClientResponseException e) {
            log.error("HTTP error fetching sales URL: {} - Status: {}", salesUrl, e.getStatusCode());
            if (e.getStatusCode().is4xxClientError()) {
                throw new InvalidUrlException("Sales URL not accessible: " + salesUrl, e);
            }
            throw new ImageNotAccessibleException("Failed to access sales URL: " + salesUrl, e);
        } catch (Exception e) {
            log.error("Error fetching images from sales URL: {}", salesUrl, e);
            return Collections.emptyList();
        }
    }

    @Override
    public int getPriority() {
        return 2;
    }

    /**
     * Validate sales URL format
     */
    private void validateSalesUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidUrlException("Sales URL cannot be empty");
        }

        String lowerUrl = url.toLowerCase();
        if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://")) {
            throw new InvalidUrlException("Sales URL must start with http:// or https://");
        }
    }

    /**
     * Fetch metadata for a single image URL
     */
    private ImageResult fetchImageMetadata(String imageUrl, long overallStartTime) {
        try {
            long imageStartTime = System.currentTimeMillis();

            // Handle relative URLs
            String fullImageUrl = imageUrl;
            if (imageUrl.startsWith("//")) {
                fullImageUrl = "https:" + imageUrl;
            }

            byte[] imageBytes = webClient.get()
                .uri(fullImageUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofMillis(50)) // Quick timeout for individual images
                .block();

            long loadingTime = System.currentTimeMillis() - imageStartTime;

            if (imageBytes != null && imageBytes.length > 0) {
                String resolution = performanceMetricsService.getImageResolution(imageBytes);
                long fileSize = imageBytes.length;

                return new ImageResult(
                    fullImageUrl,
                    ImageSource.SALES_URL,
                    loadingTime,
                    resolution,
                    fileSize
                );
            }
        } catch (Exception e) {
            log.debug("Could not fetch metadata for image: {}", imageUrl);
        }

        // Return minimal result if metadata fetch fails
        long loadingTime = System.currentTimeMillis() - overallStartTime;
        return new ImageResult(
            imageUrl,
            ImageSource.SALES_URL,
            loadingTime,
            "unknown",
            0L
        );
    }
}