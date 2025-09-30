package com.example.imagefetch.strategy;

import com.example.imagefetch.dto.ImageFetchRequest;
import com.example.imagefetch.dto.ImageResult;
import com.example.imagefetch.dto.ImageSource;
import com.example.imagefetch.dto.SalesChannel;
import com.example.imagefetch.service.PerformanceMetricsService;
import com.example.imagefetch.util.HtmlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelSearchImageFetchStrategy implements ImageFetchStrategy {

    private final WebClient webClient;
    private final PerformanceMetricsService performanceMetricsService;

    @Value("${image-fetch.strategy.channel-search.timeout:300}")
    private int timeoutMs;

    @Value("${image-fetch.max-results:3}")
    private int maxResults;

    // Rate limiting: 5 requests/second = 200ms between requests
    private static final long MIN_REQUEST_INTERVAL_MS = 200;
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
            String searchUrl = buildSearchUrl(channel, query);

            log.debug("Channel search URL: {}", searchUrl);

            long startTime = System.currentTimeMillis();

            // Fetch search results HTML
            String html = webClient.get()
                .uri(searchUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();

            if (html == null || html.isBlank()) {
                log.warn("Empty HTML from channel search: {}", channel);
                return Collections.emptyList();
            }

            // Parse search results to extract image URLs
            List<String> imageUrls = parseSearchResults(channel, html);

            if (imageUrls.isEmpty()) {
                log.warn("No images found in channel search: {}", channel);
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

    /**
     * Build channel-specific search URL
     */
    private String buildSearchUrl(SalesChannel channel, String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        return switch (channel) {
            case NAVER -> "https://search.shopping.naver.com/search/all?query=" + encodedQuery;
            case GMARKET -> "https://www.gmarket.co.kr/n/search?keyword=" + encodedQuery;
            case COUPANG -> "https://www.coupang.com/np/search?q=" + encodedQuery;
            case ELEVENST -> "https://search.11st.co.kr/Search.tmall?kwd=" + encodedQuery;
            case AUCTION -> "https://browse.auction.co.kr/search?keyword=" + encodedQuery;
        };
    }

    /**
     * Parse search results HTML to extract image URLs
     */
    private List<String> parseSearchResults(SalesChannel channel, String html) {
        try {
            Document doc = Jsoup.parse(html);
            List<String> imageUrls = new ArrayList<>();

            Elements imageElements = switch (channel) {
                case NAVER -> extractNaverImages(doc);
                case GMARKET -> extractGmarketImages(doc);
                case COUPANG -> extractCoupangImages(doc);
                case ELEVENST -> extractElevenstImages(doc);
                case AUCTION -> extractAuctionImages(doc);
            };

            for (Element img : imageElements) {
                String src = img.attr("data-src");
                if (src.isBlank()) {
                    src = img.attr("src");
                }
                if (src.isBlank()) {
                    src = img.attr("data-original");
                }

                if (!src.isBlank() && isValidImageUrl(src)) {
                    imageUrls.add(src);
                    log.debug("Found search result image: {}", src);
                }
            }

            log.debug("Extracted {} images from {} search results", imageUrls.size(), channel);
            return imageUrls;
        } catch (Exception e) {
            log.error("Error parsing search results for channel: {}", channel, e);
            return Collections.emptyList();
        }
    }

    /**
     * Extract images from Naver Shopping search results
     */
    private Elements extractNaverImages(Document doc) {
        // Try multiple selectors for Naver Shopping
        Elements images = doc.select("div.product_list_item img.thumbnail");
        if (images.isEmpty()) {
            images = doc.select(".img_area img");
        }
        if (images.isEmpty()) {
            images = doc.select("img[data-src*='shopping.pstatic.net']");
        }
        if (images.isEmpty()) {
            images = doc.select(".basicList_img_area__j0bI4 img");
        }
        return images;
    }

    /**
     * Extract images from G-Market search results
     */
    private Elements extractGmarketImages(Document doc) {
        Elements images = doc.select(".box__item-container img.image__item");
        if (images.isEmpty()) {
            images = doc.select(".thumb img");
        }
        return images;
    }

    /**
     * Extract images from Coupang search results
     */
    private Elements extractCoupangImages(Document doc) {
        Elements images = doc.select(".search-product-wrap img");
        if (images.isEmpty()) {
            images = doc.select("dt.image img");
        }
        return images;
    }

    /**
     * Extract images from 11st search results
     */
    private Elements extractElevenstImages(Document doc) {
        Elements images = doc.select(".c-card-item__img img");
        if (images.isEmpty()) {
            images = doc.select(".prd_img img");
        }
        return images;
    }

    /**
     * Extract images from Auction search results
     */
    private Elements extractAuctionImages(Document doc) {
        Elements images = doc.select(".item_img img");
        if (images.isEmpty()) {
            images = doc.select(".component-item_image img");
        }
        return images;
    }

    /**
     * Validate if URL looks like a valid image URL
     */
    private boolean isValidImageUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        String lowerUrl = url.toLowerCase();

        // Filter out tracking pixels and tiny images
        if (lowerUrl.contains("1x1") || lowerUrl.contains("pixel") ||
            lowerUrl.contains("tracking") || lowerUrl.contains("blank")) {
            return false;
        }

        // Filter out icons and logos
        if (lowerUrl.contains("icon") || lowerUrl.contains("logo.")) {
            return false;
        }

        return true;
    }
}