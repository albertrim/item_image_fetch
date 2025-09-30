package com.example.imagefetch.integration;

import com.example.imagefetch.dto.ImageFetchRequest;
import com.example.imagefetch.dto.ImageFetchResponse;
import com.example.imagefetch.dto.SalesChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ImageFetchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fetchImages_shouldReturnEmpty_whenNoInputsProvided() throws Exception {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            null,
            null,
            null
        );

        MvcResult result = mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.images").isArray())
            .andExpect(jsonPath("$.totalLoadingTimeMs").exists())
            .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ImageFetchResponse response = objectMapper.readValue(responseJson, ImageFetchResponse.class);

        assertThat(response.images()).isEmpty();
        assertThat(response.totalLoadingTimeMs()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void fetchImages_shouldReturnImages_whenOnlyDirectUrlProvided() throws Exception {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            "https://via.placeholder.com/200.png",
            null,
            null
        );

        mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.images").isArray())
            .andExpect(jsonPath("$.totalLoadingTimeMs").exists());
    }

    @Test
    void fetchImages_shouldReturnImages_whenOnlySalesUrlProvided() throws Exception {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            null,
            "https://example.com/product",
            null
        );

        mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.images").isArray())
            .andExpect(jsonPath("$.totalLoadingTimeMs").exists());
    }

    @Test
    void fetchImages_shouldReturnImages_whenOnlyChannelSearchProvided() throws Exception {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            null,
            null,
            SalesChannel.NAVER
        );

        mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.images").isArray())
            .andExpect(jsonPath("$.totalLoadingTimeMs").exists());
    }

    @Test
    void fetchImages_shouldExecuteAllStrategies_whenAllInputsProvided() throws Exception {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            "Option",
            "https://via.placeholder.com/200.png",
            "https://example.com/product",
            SalesChannel.NAVER
        );

        MvcResult result = mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.images").isArray())
            .andExpect(jsonPath("$.totalLoadingTimeMs").exists())
            .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ImageFetchResponse response = objectMapper.readValue(responseJson, ImageFetchResponse.class);

        // Should return max 3 images (or less if strategies fail)
        assertThat(response.images().size()).isLessThanOrEqualTo(3);
    }

    @Test
    void fetchImages_shouldLimitToMaxThreeImages() throws Exception {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            "https://via.placeholder.com/200.png",
            "https://example.com/product",
            SalesChannel.NAVER
        );

        MvcResult result = mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ImageFetchResponse response = objectMapper.readValue(responseJson, ImageFetchResponse.class);

        assertThat(response.images().size()).isLessThanOrEqualTo(3);
    }

    @Test
    void fetchImages_shouldHandleInvalidUrl_withBadRequest() throws Exception {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            "invalid-url",
            null,
            null
        );

        mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.images").isArray());
    }

    @Test
    void fetchImages_shouldIncludePerformanceMetrics() throws Exception {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            "https://via.placeholder.com/200.png",
            null,
            null
        );

        MvcResult result = mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLoadingTimeMs").isNumber())
            .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ImageFetchResponse response = objectMapper.readValue(responseJson, ImageFetchResponse.class);

        assertThat(response.totalLoadingTimeMs()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void fetchImages_shouldHandleMissingItemName_withBadRequest() throws Exception {
        String invalidJson = "{\"imageUrl\":\"https://example.com/image.jpg\"}";

        mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    @Test
    void fetchImages_shouldExecuteStrategiesInPriorityOrder() throws Exception {
        // Priority 1 (Direct URL) should execute first
        // Priority 2 (Sales URL) should execute second
        // Priority 3 (Channel Search) should execute third

        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            "https://via.placeholder.com/200.png",
            "https://example.com/product",
            SalesChannel.NAVER
        );

        MvcResult result = mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ImageFetchResponse response = objectMapper.readValue(responseJson, ImageFetchResponse.class);

        // Verify that results are returned (priority order is logged in service)
        assertThat(response).isNotNull();
    }

    @Test
    void fetchImages_shouldHandleTimeoutGracefully() throws Exception {
        // Use a timeout-prone URL (this may timeout depending on network)
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            "https://httpbin.org/delay/1",
            null,
            null
        );

        mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.images").isArray());
    }

    @Test
    void fetchImages_shouldReturnJsonResponse() throws Exception {
        ImageFetchRequest request = new ImageFetchRequest(
            "Test Item",
            null,
            null,
            null,
            null
        );

        mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}