package com.example.imagefetch.controller;

import com.example.imagefetch.dto.ImageFetchRequest;
import com.example.imagefetch.dto.ImageFetchResponse;
import com.example.imagefetch.dto.ImageResult;
import com.example.imagefetch.dto.ImageSource;
import com.example.imagefetch.service.ImageCollectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageFetchController.class)
class ImageFetchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ImageCollectionService imageCollectionService;

    @Test
    void testFetchImages_Success() throws Exception {
        ImageFetchRequest request = new ImageFetchRequest(
            "Samsung Galaxy S24",
            "Titanium Gray",
            "https://example.com/image.jpg",
            null,
            null
        );

        ImageResult result = new ImageResult(
            "https://example.com/image.jpg",
            ImageSource.DIRECT,
            234L,
            "800x600",
            148480L
        );

        ImageFetchResponse response = new ImageFetchResponse(234L, List.of(result));

        when(imageCollectionService.fetchImages(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLoadingTimeMs").value(234))
            .andExpect(jsonPath("$.images[0].url").value("https://example.com/image.jpg"))
            .andExpect(jsonPath("$.images[0].source").value("DIRECT"));
    }

    @Test
    void testFetchImages_MissingItemName() throws Exception {
        ImageFetchRequest request = new ImageFetchRequest(
            null,
            "Titanium Gray",
            "https://example.com/image.jpg",
            null,
            null
        );

        mockMvc.perform(post("/api/v1/images/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}