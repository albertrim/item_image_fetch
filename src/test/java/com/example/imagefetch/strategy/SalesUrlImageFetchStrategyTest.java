package com.example.imagefetch.strategy;

import com.example.imagefetch.dto.ImageFetchRequest;
import com.example.imagefetch.dto.ImageResult;
import com.example.imagefetch.dto.ImageSource;
import com.example.imagefetch.exception.InvalidUrlException;
import com.example.imagefetch.service.PerformanceMetricsService;
import com.example.imagefetch.util.HtmlParser;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesUrlImageFetchStrategyTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private HtmlParser htmlParser;

    @Mock
    private PerformanceMetricsService performanceMetricsService;

    private SalesUrlImageFetchStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new SalesUrlImageFetchStrategy(webClient, htmlParser, performanceMetricsService);
        ReflectionTestUtils.setField(strategy, "timeoutMs", 200);
        ReflectionTestUtils.setField(strategy, "maxResults", 3);
    }

    @Test
    void canHandle_shouldReturnTrue_whenSalesUrlProvided() {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            null,
            "https://example.com/product",
            null
        );

        boolean result = strategy.canHandle(request);

        assertThat(result).isTrue();
    }

    @Test
    void canHandle_shouldReturnFalse_whenSalesUrlNull() {
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
    void canHandle_shouldReturnFalse_whenSalesUrlBlank() {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            null,
            "   ",
            null
        );

        boolean result = strategy.canHandle(request);

        assertThat(result).isFalse();
    }

    @Test
    void fetchImages_shouldReturnEmptyList_whenCannotHandle() {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            null,
            null,
            null
        );

        List<ImageResult> results = strategy.fetchImages(request);

        assertThat(results).isEmpty();
    }

    @Test
    void fetchImages_shouldThrowException_whenInvalidUrl() {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            null,
            "invalid-url",
            null
        );

        assertThatThrownBy(() -> strategy.fetchImages(request))
            .isInstanceOf(InvalidUrlException.class)
            .hasMessageContaining("must start with http");
    }

    @Test
    void fetchImages_shouldReturnImages_whenHtmlContainsImages() {
        // Given
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            null,
            "https://example.com/product",
            null
        );

        String mockHtml = "<html><body><img src='https://example.com/image1.jpg'/></body></html>";
        List<String> mockImageUrls = List.of("https://example.com/image1.jpg");
        byte[] mockImageBytes = new byte[]{1, 2, 3};

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockHtml));
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just(mockImageBytes));

        when(htmlParser.selectRepresentativeImages(mockHtml, 3)).thenReturn(mockImageUrls);
        when(performanceMetricsService.getImageResolution(mockImageBytes)).thenReturn("100x100");

        // When
        List<ImageResult> results = strategy.fetchImages(request);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).source()).isEqualTo(ImageSource.SALES_URL);
        assertThat(results.get(0).url()).isEqualTo("https://example.com/image1.jpg");
        assertThat(results.get(0).fileSizeBytes()).isEqualTo(3);

        verify(htmlParser).selectRepresentativeImages(mockHtml, 3);
    }

    @Test
    void fetchImages_shouldReturnEmptyList_whenNoImagesFound() {
        // Given
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            null,
            "https://example.com/product",
            null
        );

        String mockHtml = "<html><body>No images here</body></html>";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockHtml));

        when(htmlParser.selectRepresentativeImages(mockHtml, 3)).thenReturn(List.of());

        // When
        List<ImageResult> results = strategy.fetchImages(request);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void getPriority_shouldReturnTwo() {
        assertThat(strategy.getPriority()).isEqualTo(2);
    }

    @Test
    void fetchImages_shouldHandleProtocolRelativeUrls() {
        // Given
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            null,
            "https://example.com/product",
            null
        );

        String mockHtml = "<html><body><img src='//example.com/image.jpg'/></body></html>";
        List<String> mockImageUrls = List.of("//example.com/image.jpg");
        byte[] mockImageBytes = new byte[]{1, 2, 3};

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockHtml));
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just(mockImageBytes));

        when(htmlParser.selectRepresentativeImages(mockHtml, 3)).thenReturn(mockImageUrls);
        when(performanceMetricsService.getImageResolution(mockImageBytes)).thenReturn("100x100");

        // When
        List<ImageResult> results = strategy.fetchImages(request);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).url()).isEqualTo("https://example.com/image.jpg");
    }
}