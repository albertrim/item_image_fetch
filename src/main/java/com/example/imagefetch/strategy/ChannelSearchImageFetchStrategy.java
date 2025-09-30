package com.example.imagefetch.strategy;

import com.example.imagefetch.dto.ImageFetchRequest;
import com.example.imagefetch.dto.ImageResult;
import com.example.imagefetch.dto.ImageSource;
import com.example.imagefetch.dto.SalesChannel;
import com.example.imagefetch.service.GoogleImageSearchService;
import com.example.imagefetch.service.PerformanceMetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class ChannelSearchImageFetchStrategy implements ImageFetchStrategy {

    private final GoogleImageSearchService googleImageSearchService;
    private final PerformanceMetricsService performanceMetricsService;

    public ChannelSearchImageFetchStrategy(GoogleImageSearchService googleImageSearchService,
                                           PerformanceMetricsService performanceMetricsService) {
        this.googleImageSearchService = googleImageSearchService;
        this.performanceMetricsService = performanceMetricsService;
    }

    @Value("${image-fetch.strategy.channel-search.timeout:300}")
    private int timeoutMs;

    @Value("${image-fetch.max-results:3}")
    private int maxResults;

    // Rate limiting: 1 request/second = 1000ms between requests (enhanced for anti-crawling)
    private static final long MIN_REQUEST_INTERVAL_MS = 1000;
    private final AtomicLong lastRequestTime = new AtomicLong(0);

    @Override
    public boolean canHandle(ImageFetchRequest request) {
        return request.salesChannel() != null &&
               (request.itemName() != null && !request.itemName().isBlank());
    }

    @Override
    public List<ImageResult> fetchImages(ImageFetchRequest request) {
        if (!canHandle(request)) {
            return Collections.emptyList();
        }

        SalesChannel channel = request.salesChannel();
        log.debug("Fetching images from channel search: {}", channel);

        try {
            // Apply rate limiting
            applyRateLimiting();

            // Build search query
            String query = buildSearchQuery(request);

            log.debug("Searching images for query: {} on channel: {}", query, channel);

            long startTime = System.currentTimeMillis();

            // Use Google Image Search API
            // Add "product" keyword to improve search relevance
            List<String> imageUrls = googleImageSearchService.searchImages(query + " product");

            if (imageUrls.isEmpty()) {
                log.warn("No images found for query: {} on channel: {}", query, channel);
                return Collections.emptyList();
            }

            // Limit to top N images
            List<String> topImages = imageUrls.stream()
                .limit(maxResults)
                .toList();

            // Build results with metadata
            List<ImageResult> results = new ArrayList<>();
            for (String imageUrl : topImages) {
                long loadingTime = System.currentTimeMillis() - startTime;

                // Handle protocol-relative URLs
                String fullImageUrl = imageUrl;
                if (imageUrl.startsWith("//")) {
                    fullImageUrl = "https:" + imageUrl;
                }

                ImageResult result = new ImageResult(
                    fullImageUrl,
                    ImageSource.CHANNEL_SEARCH,
                    loadingTime,
                    "unknown",
                    0L
                );
                results.add(result);
            }

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("Successfully fetched {} images from channel {} in {}ms", results.size(), channel, totalTime);
            return results;

        } catch (Exception e) {
            log.error("Error fetching images from channel search: {}", channel, e);
            return Collections.emptyList();
        }
    }

    @Override
    public int getPriority() {
        return 3;
    }

    /**
     * Apply rate limiting: max 5 requests/second
     */
    private void applyRateLimiting() {
        long now = System.currentTimeMillis();
        long lastRequest = lastRequestTime.get();
        long timeSinceLastRequest = now - lastRequest;

        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
            long sleepTime = MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest;
            try {
                log.debug("Rate limiting: sleeping for {}ms", sleepTime);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Rate limiting sleep interrupted", e);
            }
        }

        lastRequestTime.set(System.currentTimeMillis());
    }

    /**
     * Build search query from item name and option name
     */
    private String buildSearchQuery(ImageFetchRequest request) {
        StringBuilder query = new StringBuilder();
        query.append(request.itemName());

        if (request.optionName() != null && !request.optionName().isBlank()) {
            query.append(" ").append(request.optionName());
        }

        return query.toString().trim();
    }

}