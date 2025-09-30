package com.example.imagefetch.dto;

import jakarta.validation.constraints.NotBlank;

public record ImageFetchRequest(
    @NotBlank(message = "itemName is required")
    String itemName,
    String optionName,
    String imageUrl,
    String salesUrl,
    SalesChannel salesChannel
) {
}