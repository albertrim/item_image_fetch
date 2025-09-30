package com.example.imagefetch.strategy;

import com.example.imagefetch.dto.ImageFetchRequest;
import com.example.imagefetch.dto.ImageResult;
import java.util.List;

public interface ImageFetchStrategy {
    boolean canHandle(ImageFetchRequest request);
    List<ImageResult> fetchImages(ImageFetchRequest request);
    int getPriority();
}