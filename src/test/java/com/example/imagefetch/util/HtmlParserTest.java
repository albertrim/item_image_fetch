package com.example.imagefetch.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlParserTest {

    private HtmlParser htmlParser;

    @BeforeEach
    void setUp() {
        htmlParser = new HtmlParser();
    }

    @Test
    void extractOgImage_shouldReturnImageUrl_whenOgTagExists() {
        String html = """
            <html>
            <head>
                <meta property="og:image" content="https://example.com/image.jpg" />
            </head>
            </html>
            """;

        Optional<String> result = htmlParser.extractOgImage(html);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    void extractOgImage_shouldReturnEmpty_whenNoOgTag() {
        String html = """
            <html>
            <head>
                <title>Test</title>
            </head>
            </html>
            """;

        Optional<String> result = htmlParser.extractOgImage(html);

        assertThat(result).isEmpty();
    }

    @Test
    void extractTwitterImage_shouldReturnImageUrl_whenTwitterTagExists() {
        String html = """
            <html>
            <head>
                <meta name="twitter:image" content="https://example.com/twitter.jpg" />
            </head>
            </html>
            """;

        Optional<String> result = htmlParser.extractTwitterImage(html);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("https://example.com/twitter.jpg");
    }

    @Test
    void extractTwitterImage_shouldReturnEmpty_whenNoTwitterTag() {
        String html = """
            <html>
            <head>
                <title>Test</title>
            </head>
            </html>
            """;

        Optional<String> result = htmlParser.extractTwitterImage(html);

        assertThat(result).isEmpty();
    }

    @Test
    void extractItemImages_shouldReturnImageUrls_whenImagesExist() {
        String html = """
            <html>
            <body>
                <img src="https://example.com/image1.jpg" />
                <img src="https://example.com/image2.png" />
                <img class="product-image" src="https://example.com/product.jpg" />
            </body>
            </html>
            """;

        List<String> results = htmlParser.extractItemImages(html);

        assertThat(results).hasSize(3);
        assertThat(results).contains(
            "https://example.com/image1.jpg",
            "https://example.com/image2.png",
            "https://example.com/product.jpg"
        );
    }

    @Test
    void extractItemImages_shouldHandleDataSrc() {
        String html = """
            <html>
            <body>
                <img data-src="https://example.com/lazy-image.jpg" />
            </body>
            </html>
            """;

        List<String> results = htmlParser.extractItemImages(html);

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo("https://example.com/lazy-image.jpg");
    }

    @Test
    void extractItemImages_shouldFilterOutInvalidUrls() {
        String html = """
            <html>
            <body>
                <img src="https://example.com/valid.jpg" />
                <img src="https://example.com/icon.png" />
                <img src="https://example.com/1x1.gif" />
                <img src="https://example.com/tracking.png" />
            </body>
            </html>
            """;

        List<String> results = htmlParser.extractItemImages(html);

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo("https://example.com/valid.jpg");
    }

    @Test
    void selectRepresentativeImages_shouldPrioritizeOgImage() {
        String html = """
            <html>
            <head>
                <meta property="og:image" content="https://example.com/og-image.jpg" />
                <meta name="twitter:image" content="https://example.com/twitter-image.jpg" />
            </head>
            <body>
                <img src="https://example.com/item1.jpg" />
                <img src="https://example.com/item2.jpg" />
            </body>
            </html>
            """;

        List<String> results = htmlParser.selectRepresentativeImages(html, 3);

        assertThat(results).hasSize(3);
        assertThat(results.get(0)).isEqualTo("https://example.com/og-image.jpg");
        assertThat(results.get(1)).isEqualTo("https://example.com/twitter-image.jpg");
    }

    @Test
    void selectRepresentativeImages_shouldRespectMaxLimit() {
        String html = """
            <html>
            <head>
                <meta property="og:image" content="https://example.com/og-image.jpg" />
            </head>
            <body>
                <img src="https://example.com/item1.jpg" />
                <img src="https://example.com/item2.jpg" />
                <img src="https://example.com/item3.jpg" />
            </body>
            </html>
            """;

        List<String> results = htmlParser.selectRepresentativeImages(html, 2);

        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isEqualTo("https://example.com/og-image.jpg");
    }

    @Test
    void selectRepresentativeImages_shouldRemoveDuplicates() {
        String html = """
            <html>
            <head>
                <meta property="og:image" content="https://example.com/same-image.jpg" />
                <meta name="twitter:image" content="https://example.com/same-image.jpg" />
            </head>
            <body>
                <img src="https://example.com/same-image.jpg" />
            </body>
            </html>
            """;

        List<String> results = htmlParser.selectRepresentativeImages(html, 5);

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo("https://example.com/same-image.jpg");
    }
}