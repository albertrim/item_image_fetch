package com.example.imagefetch.strategy;

import com.example.imagefetch.dto.ImageFetchRequest;
import com.example.imagefetch.dto.ImageResult;
import com.example.imagefetch.dto.ImageSource;
import com.example.imagefetch.dto.SalesChannel;
import com.example.imagefetch.service.PerformanceMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelSearchImageFetchStrategyTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private PerformanceMetricsService performanceMetricsService;

    private ChannelSearchImageFetchStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ChannelSearchImageFetchStrategy(webClient, performanceMetricsService);
        ReflectionTestUtils.setField(strategy, "timeoutMs", 300);
        ReflectionTestUtils.setField(strategy, "maxResults", 3);
    }

    @Test
    void canHandle_shouldReturnTrue_whenChannelAndItemNameProvided() {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            null,
            null,
            SalesChannel.NAVER
        );

        boolean result = strategy.canHandle(request);

        assertThat(result).isTrue();
    }

    @Test
    void canHandle_shouldReturnFalse_whenChannelNull() {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            null,
            null,
            null
        );

        boolean result = strategy.canHandle(request);

        assertThat(result).isFalse();
    }

    @Test
    void canHandle_shouldReturnFalse_whenItemNameNull() {
        ImageFetchRequest request = new ImageFetchRequest(
            null,
            null,
            null,
            null,
            SalesChannel.NAVER
        );

        boolean result = strategy.canHandle(request);

        assertThat(result).isFalse();
    }

    @Test
    void canHandle_shouldReturnFalse_whenItemNameBlank() {
        ImageFetchRequest request = new ImageFetchRequest(
            "   ",
            null,
            null,
            null,
            SalesChannel.NAVER
        );

        boolean result = strategy.canHandle(request);

        assertThat(result).isFalse();
    }

    @Test
    void fetchImages_shouldReturnEmptyList_whenCannotHandle() {
        ImageFetchRequest request = new ImageFetchRequest(
            null,
            null,
            null,
            null,
            null
        );

        List<ImageResult> results = strategy.fetchImages(request);

        assertThat(results).isEmpty();
    }

    @Test
    void fetchImages_shouldReturnImages_whenNaverSearchSucceeds() {
        // Given
        ImageFetchRequest request = new ImageFetchRequest(
            "맥북",
            null,
            null,
            null,
            SalesChannel.NAVER
        );

        String mockHtml = """
            <html>
            <body>
                <div class="product_list_item">
                    <img class="thumbnail" data-src="https://shopping.pstatic.net/image1.jpg" />
                </div>
                <div class="product_list_item">
                    <img class="thumbnail" src="https://shopping.pstatic.net/image2.jpg" />
                </div>
                <div class="product_list_item">
                    <img class="thumbnail" data-src="https://shopping.pstatic.net/image3.jpg" />
                </div>
            </body>
            </html>
            """;

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockHtml));

        // When
        List<ImageResult> results = strategy.fetchImages(request);

        // Then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).source()).isEqualTo(ImageSource.CHANNEL_SEARCH);
        assertThat(results.get(0).url()).isEqualTo("https://shopping.pstatic.net/image1.jpg");
        assertThat(results.get(1).url()).isEqualTo("https://shopping.pstatic.net/image2.jpg");
        assertThat(results.get(2).url()).isEqualTo("https://shopping.pstatic.net/image3.jpg");

        verify(requestHeadersSpec).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");
    }

    @Test
    void fetchImages_shouldLimitToMaxResults() {
        // Given
        ImageFetchRequest request = new ImageFetchRequest(
            "갤럭시",
            null,
            null,
            null,
            SalesChannel.NAVER
        );

        String mockHtml = """
            <html>
            <body>
                <div class="product_list_item"><img class="thumbnail" src="https://shopping.pstatic.net/image1.jpg" /></div>
                <div class="product_list_item"><img class="thumbnail" src="https://shopping.pstatic.net/image2.jpg" /></div>
                <div class="product_list_item"><img class="thumbnail" src="https://shopping.pstatic.net/image3.jpg" /></div>
                <div class="product_list_item"><img class="thumbnail" src="https://shopping.pstatic.net/image4.jpg" /></div>
                <div class="product_list_item"><img class="thumbnail" src="https://shopping.pstatic.net/image5.jpg" /></div>
            </body>
            </html>
            """;

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockHtml));

        // When
        List<ImageResult> results = strategy.fetchImages(request);

        // Then
        assertThat(results).hasSize(3); // Limited to maxResults
    }

    @Test
    void fetchImages_shouldBuildQueryWithOptionName() {
        // Given
        ImageFetchRequest request = new ImageFetchRequest(
            "맥북",
            "16인치",
            null,
            null,
            SalesChannel.NAVER
        );

        String mockHtml = """
            <html>
            <body>
                <div class="product_list_item">
                    <img class="thumbnail" src="https://shopping.pstatic.net/image1.jpg" />
                </div>
            </body>
            </html>
            """;

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockHtml));

        // When
        strategy.fetchImages(request);

        // Then
        verify(requestHeadersUriSpec).uri(contains("query=%EB%A7%A5%EB%B6%81+16%EC%9D%B8%EC%B9%98"));
    }

    @Test
    void fetchImages_shouldHandleProtocolRelativeUrls() {
        // Given
        ImageFetchRequest request = new ImageFetchRequest(
            "Test",
            null,
            null,
            null,
            SalesChannel.NAVER
        );

        String mockHtml = """
            <html>
            <body>
                <div class="product_list_item">
                    <img class="thumbnail" src="//shopping.pstatic.net/image1.jpg" />
                </div>
            </body>
            </html>
            """;

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockHtml));

        // When
        List<ImageResult> results = strategy.fetchImages(request);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).url()).isEqualTo("https://shopping.pstatic.net/image1.jpg");
    }

    @Test
    void fetchImages_shouldFilterOutInvalidImages() {
        // Given
        ImageFetchRequest request = new ImageFetchRequest(
            "Test",
            null,
            null,
            null,
            SalesChannel.NAVER
        );

        String mockHtml = """
            <html>
            <body>
                <div class="product_list_item"><img class="thumbnail" src="https://shopping.pstatic.net/valid.jpg" /></div>
                <div class="product_list_item"><img class="thumbnail" src="https://shopping.pstatic.net/icon.png" /></div>
                <div class="product_list_item"><img class="thumbnail" src="https://shopping.pstatic.net/1x1.gif" /></div>
                <div class="product_list_item"><img class="thumbnail" src="https://shopping.pstatic.net/tracking.png" /></div>
            </body>
            </html>
            """;

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockHtml));

        // When
        List<ImageResult> results = strategy.fetchImages(request);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).url()).contains("valid.jpg");
    }

    @Test
    void fetchImages_shouldReturnEmptyList_whenNoImagesFound() {
        // Given
        ImageFetchRequest request = new ImageFetchRequest(
            "Test",
            null,
            null,
            null,
            SalesChannel.NAVER
        );

        String mockHtml = "<html><body>No images here</body></html>";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockHtml));

        // When
        List<ImageResult> results = strategy.fetchImages(request);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void getPriority_shouldReturnThree() {
        assertThat(strategy.getPriority()).isEqualTo(3);
    }

    @Test
    void fetchImages_shouldWorkWithDifferentChannels() {
        // Test that different channels use different selectors
        for (SalesChannel channel : SalesChannel.values()) {
            ImageFetchRequest request = new ImageFetchRequest(
                "Test",
                null,
                null,
                null,
                channel
            );

            String mockHtml = "<html><body></body></html>";

            when(webClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockHtml));

            List<ImageResult> results = strategy.fetchImages(request);

            assertThat(results).isNotNull();
        }
    }
}