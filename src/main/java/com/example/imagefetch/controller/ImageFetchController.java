package com.example.imagefetch.controller;

import com.example.imagefetch.dto.ImageFetchRequest;
import com.example.imagefetch.dto.ImageFetchResponse;
import com.example.imagefetch.service.ImageCollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageFetchController {

    private final ImageCollectionService imageCollectionService;

    @PostMapping("/fetch")
    public ResponseEntity<ImageFetchResponse> fetchImages(@Valid @RequestBody ImageFetchRequest request) {
        log.info("Received image fetch request for item: {}", request.itemName());
        ImageFetchResponse response = imageCollectionService.fetchImages(request);
        return ResponseEntity.ok(response);
    }
}