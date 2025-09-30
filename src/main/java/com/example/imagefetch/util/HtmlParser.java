package com.example.imagefetch.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for parsing HTML and extracting image URLs
 */
@Slf4j
@Component
public class HtmlParser {

    /**
     * Extract Open Graph image URL from HTML
     *
     * @param html HTML content
     * @return Optional containing OG image URL if found
     */
    public Optional<String> extractOgImage(String html) {
        try {
            Document doc = Jsoup.parse(html);
            Element ogImage = doc.selectFirst("meta[property=og:image]");

            if (ogImage != null) {
                String imageUrl = ogImage.attr("content");
                if (!imageUrl.isBlank()) {
                    log.debug("Found OG image: {}", imageUrl);
                    return Optional.of(imageUrl);
                }
            }

            log.debug("No OG image found");
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error parsing OG image from HTML", e);
            return Optional.empty();
        }
    }

    /**
     * Extract Twitter Card image URL from HTML
     *
     * @param html HTML content
     * @return Optional containing Twitter image URL if found
     */
    public Optional<String> extractTwitterImage(String html) {
        try {
            Document doc = Jsoup.parse(html);
            Element twitterImage = doc.selectFirst("meta[name=twitter:image], meta[property=twitter:image]");

            if (twitterImage != null) {
                String imageUrl = twitterImage.attr("content");
                if (!imageUrl.isBlank()) {
                    log.debug("Found Twitter image: {}", imageUrl);
                    return Optional.of(imageUrl);
                }
            }

            log.debug("No Twitter image found");
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error parsing Twitter image from HTML", e);
            return Optional.empty();
        }
    }

    /**
     * Extract item images from HTML using common img selectors
     *
     * @param html HTML content
     * @return List of image URLs found in the HTML
     */
    public List<String> extractItemImages(String html) {
        try {
            Document doc = Jsoup.parse(html);
            List<String> imageUrls = new ArrayList<>();

            // Try to find images in common product image containers
            Elements images = doc.select(
                "img.product-image, " +
                "img.item-image, " +
                "img[itemprop=image], " +
                ".product-detail img, " +
                ".item-detail img, " +
                ".product-images img, " +
                "img"
            );

            for (Element img : images) {
                String src = img.attr("src");
                if (src.isBlank()) {
                    src = img.attr("data-src");
                }
                if (src.isBlank()) {
                    src = img.attr("data-original");
                }

                if (!src.isBlank() && isValidImageUrl(src)) {
                    imageUrls.add(src);
                    log.debug("Found item image: {}", src);
                }
            }

            log.debug("Extracted {} item images from HTML", imageUrls.size());
            return imageUrls.stream().distinct().collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error extracting item images from HTML", e);
            return List.of();
        }
    }

    /**
     * Select representative images from HTML using multiple strategies
     * Priority: OG image > Twitter image > Item images
     *
     * @param html HTML content
     * @param maxImages Maximum number of images to return
     * @return List of representative image URLs
     */
    public List<String> selectRepresentativeImages(String html, int maxImages) {
        List<String> representativeImages = new ArrayList<>();

        // Strategy 1: Try OG image
        extractOgImage(html).ifPresent(representativeImages::add);

        // Strategy 2: Try Twitter image (if different from OG)
        if (representativeImages.size() < maxImages) {
            extractTwitterImage(html).ifPresent(url -> {
                if (!representativeImages.contains(url)) {
                    representativeImages.add(url);
                }
            });
        }

        // Strategy 3: Add item images
        if (representativeImages.size() < maxImages) {
            List<String> itemImages = extractItemImages(html);
            for (String imgUrl : itemImages) {
                if (representativeImages.size() >= maxImages) {
                    break;
                }
                if (!representativeImages.contains(imgUrl)) {
                    representativeImages.add(imgUrl);
                }
            }
        }

        log.info("Selected {} representative images from HTML", representativeImages.size());
        return representativeImages;
    }

    /**
     * Validate if URL looks like a valid image URL
     *
     * @param url URL to validate
     * @return true if URL appears to be valid
     */
    private boolean isValidImageUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        // Filter out common non-image URLs
        String lowerUrl = url.toLowerCase();

        // Must start with http or // (protocol-relative)
        if (!lowerUrl.startsWith("http") && !lowerUrl.startsWith("//")) {
            // Could be relative URL - still valid
            if (!lowerUrl.startsWith("/")) {
                return false;
            }
        }

        // Filter out tracking pixels and tiny images
        if (lowerUrl.contains("1x1") || lowerUrl.contains("pixel") || lowerUrl.contains("tracking")) {
            return false;
        }

        // Filter out icons and logos (usually small)
        if (lowerUrl.contains("icon") || lowerUrl.contains("logo.")) {
            return false;
        }

        return true;
    }
}