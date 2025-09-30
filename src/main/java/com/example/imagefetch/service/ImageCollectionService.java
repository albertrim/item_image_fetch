package com.example.imagefetch.service;

import com.example.imagefetch.dto.ImageFetchRequest;
import com.example.imagefetch.dto.ImageFetchResponse;
import com.example.imagefetch.dto.ImageResult;
import com.example.imagefetch.strategy.ImageFetchStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCollectionService {

    private final List<ImageFetchStrategy> strategies;

    @Value("${image-fetch.max-results:3}")
    private int maxResults;

    public ImageFetchResponse fetchImages(ImageFetchRequest request) {
        log.info("Fetching images for item: {}", request.itemName());
        long startTime = System.currentTimeMillis();

        List<ImageResult> allResults = new ArrayList<>();

        // Execute strategies in priority order
        strategies.stream()
            .sorted(Comparator.comparingInt(ImageFetchStrategy::getPriority))
            .filter(strategy -> strategy.canHandle(request))
            .forEach(strategy -> {
                try {
                    log.debug("Executing strategy: {}", strategy.getClass().getSimpleName());
                    List<ImageResult> results = strategy.fetchImages(request);
                    allResults.addAll(results);
                } catch (Exception e) {
                    log.error("Strategy {} failed", strategy.getClass().getSimpleName(), e);
                }
            });

        // Select top N images
        List<ImageResult> selectedImages = selectTopNImages(allResults);

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("Image fetch completed in {}ms, found {} images", totalTime, selectedImages.size());

        return new ImageFetchResponse(totalTime, selectedImages);
    }

    private List<ImageResult> selectTopNImages(List<ImageResult> results) {
        return results.stream()
            .limit(maxResults)
            .toList();
    }
}