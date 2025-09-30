package com.example.imagefetch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Google Custom Search API service for image search
 * Uses Google Custom Search JSON API to search for product images
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleImageSearchService {

    private final WebClient webClient;

    @Value("${image-fetch.google-search.api-key}")
    private String apiKey;

    @Value("${image-fetch.google-search.cx}")
    private String cx;

    @Value("${image-fetch.google-search.enabled:true}")
    private boolean enabled;

    @Value("${image-fetch.max-results:3}")
    private int maxResults;

    private static final String GOOGLE_API_URL = "https://www.googleapis.com/customsearch/v1";
    private static final int TIMEOUT_MS = 3000;

    /**
     * Search for images using Google Custom Search API
     *
     * @param query Search query (item name + option name)
     * @return List of image URLs
     */
    public List<String> searchImages(String query) {
        if (!enabled) {
            log.warn("Google Image Search is disabled");
            return Collections.emptyList();
        }

        if (apiKey == null || apiKey.equals("YOUR_API_KEY_HERE")) {
            log.warn("Google API key not configured");
            return Collections.emptyList();
        }

        if (cx == null || cx.equals("YOUR_CX_HERE")) {
            log.warn("Google Custom Search Engine ID (CX) not configured");
            return Collections.emptyList();
        }

        try {
            log.debug("Searching Google Images for query: {}", query);

            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            // Call Google Custom Search API
            Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .scheme("https")
                    .host("www.googleapis.com")
                    .path("/customsearch/v1")
                    .queryParam("key", apiKey)
                    .queryParam("cx", cx)
                    .queryParam("q", encodedQuery)
                    .queryParam("searchType", "image")
                    .queryParam("num", maxResults)
                    .queryParam("safe", "active")
                    .build())
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(TIMEOUT_MS))
                .block();

            if (response == null) {
                log.warn("Empty response from Google Image Search");
                return Collections.emptyList();
            }

            // Extract image URLs from response
            List<String> imageUrls = extractImageUrls(response);

            log.info("Found {} images from Google Image Search for query: {}", imageUrls.size(), query);
            return imageUrls;

        } catch (Exception e) {
            log.error("Error searching Google Images for query: {}", query, e);
            return Collections.emptyList();
        }
    }

    /**
     * Extract image URLs from Google Custom Search API response
     *
     * @param response API response map
     * @return List of image URLs
     */
    @SuppressWarnings("unchecked")
    private List<String> extractImageUrls(Map<String, Object> response) {
        List<String> imageUrls = new ArrayList<>();

        try {
            Object itemsObj = response.get("items");
            if (itemsObj instanceof List) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) itemsObj;

                for (Map<String, Object> item : items) {
                    String link = (String) item.get("link");
                    if (link != null && !link.isBlank()) {
                        imageUrls.add(link);
                        log.debug("Found Google image: {}", link);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extracting image URLs from Google response", e);
        }

        return imageUrls;
    }

    /**
     * Check if Google Image Search is properly configured
     *
     * @return true if API key and CX are configured
     */
    public boolean isConfigured() {
        return enabled &&
               apiKey != null && !apiKey.equals("YOUR_API_KEY_HERE") &&
               cx != null && !cx.equals("YOUR_CX_HERE");
    }
}