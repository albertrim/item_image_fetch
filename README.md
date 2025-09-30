# Item Image Auto-Collection

Spring Boot application for automatic item image collection with multi-priority fetching strategy.

## Project Overview
- **Goal**: Validate 3-tier priority image collection functionality
- **Tech Stack**: Java 17, Spring Boot 3.2.x, Jsoup, WebClient
- **Success Criteria**: 80%+ success rate, meet target response times (500ms/2s/3s)

## Prerequisites
- JDK 17
- Gradle 8.x (or use Gradle Wrapper)

## Setup

### Using Gradle Wrapper (if gradlew files are available)
```bash
./gradlew clean build
./gradlew bootRun
```

### Manual Setup
```bash
gradle wrapper --gradle-version 8.5
./gradlew clean build
./gradlew bootRun
```

## API Endpoints

### Fetch Images
```bash
POST /api/v1/images/fetch
Content-Type: application/json

{
  "itemName": "Samsung Galaxy S24",
  "optionName": "Titanium Gray",
  "imageUrl": "https://example.com/image.jpg",
  "salesUrl": null,
  "salesChannel": null
}
```

Response:
```json
{
  "totalLoadingTimeMs": 234,
  "images": [
    {
      "url": "https://example.com/image.jpg",
      "source": "DIRECT",
      "loadingTimeMs": 234,
      "resolution": "800x600",
      "fileSizeBytes": 148480
    }
  ]
}
```

## Testing
```bash
./gradlew test
```

## Documentation
- [PRD](./prd.md) - Product Requirements Document
- [Execution Plan](./execution.md) - Detailed technical specifications
- [Task List](./task.md) - Implementation task breakdown
- [Coding Standards](./CLAUDE.md) - Development guidelines

## Task 1 Status (Priority 1 - Direct URL)
- ✅ Project setup complete
- ✅ DTO classes implemented
- ✅ Strategy pattern foundation (DirectUrlImageFetchStrategy)
- ✅ Service layer (ImageCollectionService, PerformanceMetricsService)
- ✅ Controller and configuration
- ✅ Basic tests written
- ⏳ Pending: Gradle wrapper generation and build verification

## Next Steps
- Install Gradle or generate Gradle wrapper
- Run build and tests
- Test API with sample direct image URLs
- Proceed to Task 2 (Sales URL Crawling)