# Item Image Auto-Collection Feature - Execution Plan and Technical Specifications

## 1. Technology Stack

### 1.1 Core Technologies
- **Java 17**: LTS version, leveraging modern language features (Records, Pattern Matching, Text Blocks)
- **Spring Boot 3.2.x**: REST API, dependency injection, asynchronous processing
- **Gradle 8.x**: Build tool

### 1.2 Key Libraries
- **Jsoup 1.17.2**: HTML parsing and web scraping
- **Spring WebClient**: Asynchronous HTTP requests (Reactive)
- **Lombok**: Reduce boilerplate code
- **Apache Commons IO**: File size calculation and utilities

### 1.3 Spring Boot Rationale
- REST API endpoint construction required
- Asynchronous processing (@Async, WebClient) essential
- DI/IoC container facilitates Strategy Pattern implementation
- Configuration management and profile support
- Embedded Tomcat for rapid prototype development

---

## 2. System Architecture

### 2.1 Layered Architecture
```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│      (REST API Controller)              │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│          Service Layer                  │
│   (Business Logic & Orchestration)      │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Strategy Layer                  │
│  (Image Fetch Strategies - 3 types)     │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Utility Layer                   │
│   (HTTP Client, HTML Parser, etc)       │
└─────────────────────────────────────────┘
```

### 2.2 Priority Logic Flow
```
[Request] → ImageCollectionService
                    ↓
         ┌──────────┴──────────┐
         ↓                     ↓
    [Priority Check]    [Parallel Execution]
         ↓                     ↓
    Priority 1: DirectUrlImageFetchStrategy
         ↓ (if no URL provided)
    Priority 2: SalesUrlImageFetchStrategy
         ↓ (if no Sales URL or failed)
    Priority 3: ChannelSearchImageFetchStrategy
         ↓
    [Aggregate Results] → Max 3 Images
         ↓
    [Performance Metrics] → Response
```

---

## 3. Core Component Design

### 3.1 Controller Layer

#### ImageFetchController
```java
@RestController
@RequestMapping("/api/v1/images")
public class ImageFetchController {
    // POST /api/v1/images/fetch
    // Handle image collection requests
}
```

### 3.2 Service Layer

#### ImageCollectionService
- **Role**: Coordinate priority logic, manage asynchronous execution
- **Methods**:
  - `CompletableFuture<List<ImageResult>> fetchImages(ImageFetchRequest)`
  - `List<ImageResult> selectTop3Images(List<ImageResult>)`

#### PerformanceMetricsService
- **Role**: Measure loading time, collect image metadata
- **Methods**:
  - `long measureExecutionTime(Supplier<T>)`
  - `ImageMetadata extractMetadata(String imageUrl)`

### 3.3 Strategy Layer (Strategy Pattern)

#### ImageFetchStrategy (Interface)
```java
public interface ImageFetchStrategy {
    boolean canHandle(ImageFetchRequest request);
    List<ImageResult> fetchImages(ImageFetchRequest request);
    int getPriority(); // 1: Direct, 2: SalesUrl, 3: ChannelSearch
}
```

#### DirectUrlImageFetchStrategy
- **Priority**: 1
- **Logic**: Load directly if imageUrl is provided
- **Target Response Time**: < 500ms
- **Validation**: Verify image format (jpg, png, gif, webp)

#### SalesUrlImageFetchStrategy
- **Priority**: 2
- **Logic**: Parse HTML from salesUrl, extract meta tags
- **Target Response Time**: < 2000ms
- **Parsing Targets**:
  - `<meta property="og:image">`
  - `<meta name="twitter:image">`
  - Select representative image from `<img>` tags

#### ChannelSearchImageFetchStrategy
- **Priority**: 3
- **Logic**: Execute channel-specific search query and parse results
- **Target Response Time**: < 3000ms
- **Supported Channels**:
  - Naver Shopping (Priority)
  - G-Market, Coupang, 11st, Auction (Future expansion)

### 3.4 DTO (Data Transfer Objects)

#### ImageFetchRequest
```java
public record ImageFetchRequest(
    @NotBlank String itemName,
    String optionName,
    String imageUrl,
    String salesUrl,
    SalesChannel salesChannel
) {}

public enum SalesChannel {
    NAVER, GMARKET, COUPANG, ELEVENST, AUCTION
}
```

#### ImageResult
```java
public record ImageResult(
    String url,
    ImageSource source,
    long loadingTimeMs,
    String resolution,     // e.g., "800x600"
    long fileSizeBytes
) {}

public enum ImageSource {
    DIRECT, SALES_URL, CHANNEL_SEARCH
}
```

#### ImageFetchResponse
```java
public record ImageFetchResponse(
    long totalLoadingTimeMs,
    List<ImageResult> images
) {}
```

### 3.5 Utility Classes

#### ImageValidator
- `boolean isValidImageFormat(String url)`
- `boolean isAccessible(String url)`

#### HtmlParser
- `Optional<String> extractOgImage(String html)`
- `List<String> extractItemImages(String html)`

---

## 4. GitHub Workflow & Branch Strategy

### 4.1 Branch Management

**Main Branch**: `main`
- Production-ready code
- All features merged via Pull Requests
- Protected branch (require PR approval)

**Feature Branches**: `feature/<phase-name>`
- One branch per implementation phase
- Branched from `main`
- Merged back to `main` after completion

### 4.2 Implementation Phases with Git Workflow

#### **Phase 1: Project Setup + Priority 1 (Direct URL)**
**Branch**: `feature/phase-1-setup`

**Tasks**:
- [ ] Initial Spring Boot project setup
- [ ] Create basic package structure
- [ ] Implement DTO classes (Request, Response, Result, Enums)
- [ ] Create REST API endpoint (`/api/v1/images/fetch`)
- [ ] Implement DirectUrlImageFetchStrategy
- [ ] Basic PerformanceMetricsService
- [ ] Unit tests for Priority 1
- [ ] Configuration files (application.yml)

**Git Workflow**:
```bash
git checkout -b feature/phase-1-setup
# ... implement features ...
git add .
git commit -m "feat: implement project setup and priority 1 (direct URL)"
git push origin feature/phase-1-setup
# Create PR: feature/phase-1-setup → main
# Review & Merge
```

**Deliverable**: API capable of fetching images from direct URLs with performance metrics

---

#### **Phase 2: Priority 2 (Sales URL Crawling)**
**Branch**: `feature/phase-2-sales-url`

**Tasks**:
- [ ] Add Jsoup dependency
- [ ] Implement HtmlParser utility
- [ ] Implement SalesUrlImageFetchStrategy
  - [ ] Parse OG tags (`og:image`)
  - [ ] Parse Twitter cards (`twitter:image`)
  - [ ] Representative image selection logic
- [ ] Timeout handling (2 seconds)
- [ ] Error handling (invalid URL, inaccessible pages)
- [ ] Unit & integration tests

**Git Workflow**:
```bash
git checkout main
git pull origin main
git checkout -b feature/phase-2-sales-url
# ... implement features ...
git add .
git commit -m "feat: implement priority 2 (sales URL crawling)"
git push origin feature/phase-2-sales-url
# Create PR: feature/phase-2-sales-url → main
# Review & Merge
```

**Deliverable**: Image extraction functionality from sales page URLs

---

#### **Phase 3: Priority 3 (Channel Search)**
**Branch**: `feature/phase-3-channel-search`

**Tasks**:
- [ ] Implement ChannelSearchImageFetchStrategy
- [ ] Naver Shopping search implementation
  - [ ] Generate search query (itemName + optionName)
  - [ ] Parse search result HTML
  - [ ] Extract images from top 3 results
- [ ] G-Market search implementation (if time permits)
- [ ] Coupang search implementation (if time permits)
- [ ] Timeout handling (3 seconds)
- [ ] User-Agent configuration
- [ ] Unit & integration tests

**Git Workflow**:
```bash
git checkout main
git pull origin main
git checkout -b feature/phase-3-channel-search
# ... implement features ...
git add .
git commit -m "feat: implement priority 3 (channel search)"
git push origin feature/phase-3-channel-search
# Create PR: feature/phase-3-channel-search → main
# Review & Merge
```

**Deliverable**: Channel search-based image collection functionality

---

#### **Phase 4: Integration & Testing**
**Branch**: `feature/phase-4-integration`

**Tasks**:
- [ ] Full priority logic integration
- [ ] Asynchronous parallel processing optimization
- [ ] ImageCollectionService orchestration logic
- [ ] Global exception handler
- [ ] Performance measurement validation
- [ ] Edge case testing
  - [ ] Invalid URL input
  - [ ] Items without images
  - [ ] Network timeout scenarios
- [ ] Bug fixes
- [ ] Documentation (README, API docs)

**Git Workflow**:
```bash
git checkout main
git pull origin main
git checkout -b feature/phase-4-integration
# ... implement features ...
git add .
git commit -m "feat: integrate all strategies and final testing"
git push origin feature/phase-4-integration
# Create PR: feature/phase-4-integration → main
# Review & Merge
```

**Deliverable**: Completed prototype with full functionality and performance metrics

---

### 4.3 Pull Request Template

```markdown
## Phase: [Phase Number and Name]

### Changes
- [ ] List of implemented features
- [ ] List of tests added
- [ ] Configuration changes

### Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

### Performance
- Target response times met: [Yes/No]
- Performance metrics: [Details]

### Checklist
- [ ] Code compiles without warnings
- [ ] All tests pass
- [ ] No hardcoded values
- [ ] Error handling implemented
- [ ] Logging added
```

---

## 5. Phase-wise Implementation Details (7 Days)

### **Day 1-2: Phase 1 - Project Setup & Priority 1**
- [ ] Initial project setup (Spring Boot, Gradle)
- [ ] Create basic package structure
- [ ] Implement DTO classes (Request, Response, Result)
- [ ] Create REST API endpoint (`/api/v1/images/fetch`)
- [ ] Implement DirectUrlImageFetchStrategy
- [ ] Basic implementation of PerformanceMetricsService
- [ ] Write unit tests

**Deliverable**: API capable of responding to direct image URL input

---

### **Day 3-4: Phase 2 - Priority 2 (Sales URL Crawling)**
- [ ] Add Jsoup dependency and configuration
- [ ] Implement HtmlParser utility
- [ ] Implement SalesUrlImageFetchStrategy
  - [ ] Parse OG tags
  - [ ] Parse Twitter cards
  - [ ] Representative image selection logic
- [ ] Timeout handling (2 seconds)
- [ ] Error handling (invalid URL, inaccessible pages)
- [ ] Integration testing

**Deliverable**: Image extraction functionality from sales URLs

---

### **Day 5-6: Phase 3 - Priority 3 (Channel Search)**
- [ ] Design ChannelSearchImageFetchStrategy interface
- [ ] Implement Naver Shopping search (Priority)
  - [ ] Generate search query (itemName + optionName)
  - [ ] Parse search result HTML
  - [ ] Extract images from top 3 results
- [ ] Timeout handling (3 seconds)
- [ ] User-Agent configuration (anti-crawling countermeasure)
- [ ] Integration testing
- [ ] (If time permits) Add other channels

**Deliverable**: Channel search-based image collection functionality

---

### **Day 7: Phase 4 - Integration, Optimization, Testing**
- [ ] Full priority logic integration testing
- [ ] Asynchronous parallel processing optimization
- [ ] Performance measurement and target time validation
- [ ] Edge case testing
  - [ ] Invalid URL input
  - [ ] Items without images
  - [ ] Network timeout
- [ ] Bug fixes
- [ ] Performance measurement result documentation

**Deliverable**: Completed prototype and performance measurement report

---

## 6. REST API Detailed Specification

### 6.1 Image Fetch API

**Endpoint**: `POST /api/v1/images/fetch`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "itemName": "Samsung Galaxy S24",
  "optionName": "Titanium Gray",
  "imageUrl": "https://example.com/image.jpg",
  "salesUrl": "https://shopping.naver.com/product/123456",
  "salesChannel": "NAVER"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| itemName | String | ✓ | Item name |
| optionName | String | ✗ | Option name (color, size, etc.) |
| imageUrl | String | ✗ | Direct image URL |
| salesUrl | String | ✗ | Sales page URL |
| salesChannel | Enum | ✗ | NAVER, GMARKET, COUPANG, ELEVENST, AUCTION |

**Response** (200 OK):
```json
{
  "totalLoadingTimeMs": 1234,
  "images": [
    {
      "url": "https://example.com/image1.jpg",
      "source": "DIRECT",
      "loadingTimeMs": 234,
      "resolution": "800x800",
      "fileSizeBytes": 148480
    },
    {
      "url": "https://example.com/image2.jpg",
      "source": "SALES_URL",
      "loadingTimeMs": 456,
      "resolution": "600x600",
      "fileSizeBytes": 100352
    },
    {
      "url": "https://example.com/image3.jpg",
      "source": "CHANNEL_SEARCH",
      "loadingTimeMs": 523,
      "resolution": "1200x1200",
      "fileSizeBytes": 239616
    }
  ]
}
```

**Error Responses**:

- **400 Bad Request**: Invalid request (itemName missing)
```json
{
  "error": "INVALID_REQUEST",
  "message": "itemName is required"
}
```

- **500 Internal Server Error**: Server error
```json
{
  "error": "INTERNAL_ERROR",
  "message": "Failed to fetch images"
}
```

- **504 Gateway Timeout**: Timeout
```json
{
  "error": "TIMEOUT",
  "message": "Request timeout exceeded"
}
```

---

## 7. Project Structure

```
item-image-fetch/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── imagefetch/
│   │   │               ├── ImageFetchApplication.java
│   │   │               ├── controller/
│   │   │               │   └── ImageFetchController.java
│   │   │               ├── service/
│   │   │               │   ├── ImageCollectionService.java
│   │   │               │   └── PerformanceMetricsService.java
│   │   │               ├── strategy/
│   │   │               │   ├── ImageFetchStrategy.java
│   │   │               │   ├── DirectUrlImageFetchStrategy.java
│   │   │               │   ├── SalesUrlImageFetchStrategy.java
│   │   │               │   └── ChannelSearchImageFetchStrategy.java
│   │   │               ├── dto/
│   │   │               │   ├── ImageFetchRequest.java
│   │   │               │   ├── ImageResult.java
│   │   │               │   ├── ImageFetchResponse.java
│   │   │               │   ├── ImageSource.java
│   │   │               │   └── SalesChannel.java
│   │   │               ├── util/
│   │   │               │   ├── ImageValidator.java
│   │   │               │   └── HtmlParser.java
│   │   │               ├── exception/
│   │   │               │   ├── ImageFetchException.java
│   │   │               │   ├── InvalidUrlException.java
│   │   │               │   └── TimeoutException.java
│   │   │               └── config/
│   │   │                   ├── WebClientConfig.java
│   │   │                   └── AsyncConfig.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── imagefetch/
│                       ├── controller/
│                       ├── service/
│                       └── strategy/
├── build.gradle
├── settings.gradle
├── prd.md
├── execution.md
└── README.md
```

---

## 8. Dependency Configuration (Gradle)

### build.gradle
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.5'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-webflux' // WebClient
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // Web Scraping
    implementation 'org.jsoup:jsoup:1.17.2'

    // Utils
    implementation 'org.apache.commons:commons-lang3:3.14.0'
    implementation 'commons-io:commons-io:2.15.1'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'io.projectreactor:reactor-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

---

## 9. Configuration Files

### application.yml
```yaml
spring:
  application:
    name: item-image-fetch

server:
  port: 8080

webclient:
  timeout:
    connect: 5000  # ms
    read: 5000     # ms
    write: 5000    # ms
  max-connections: 100

image-fetch:
  strategy:
    direct-url:
      timeout: 500      # ms
    sales-url:
      timeout: 2000     # ms
    channel-search:
      timeout: 3000     # ms
  max-results: 3
  allowed-formats:
    - jpg
    - jpeg
    - png
    - gif
    - webp

logging:
  level:
    com.example.imagefetch: DEBUG
    org.springframework.web: INFO
```

---

## 10. Performance Optimization Strategies

### 10.1 Asynchronous Parallel Processing
- **CompletableFuture**: Execute multiple strategies concurrently
- **WebClient (Reactive)**: Non-blocking I/O for HTTP requests
- **@Async Annotation**: Method-level asynchronous execution

**Example Logic**:
```java
CompletableFuture<List<ImageResult>> directFuture =
    CompletableFuture.supplyAsync(() -> directStrategy.fetchImages(request));

CompletableFuture<List<ImageResult>> salesFuture =
    CompletableFuture.supplyAsync(() -> salesStrategy.fetchImages(request));

CompletableFuture<List<ImageResult>> channelFuture =
    CompletableFuture.supplyAsync(() -> channelStrategy.fetchImages(request));

CompletableFuture.allOf(directFuture, salesFuture, channelFuture).join();
```

### 10.2 Timeout Management
- Strategy-specific timeout configuration (500ms/2s/3s)
- Simple timeout with fallback to next strategy

### 10.3 Connection Pool
- WebClient Connection Pool configuration
- Max connections: 100
- Keep-Alive configuration

### 10.4 Image Metadata Optimization
- HEAD request to check Content-Length, Content-Type first
- Collect metadata without full image download

---

## 11. Error Handling Strategy

### 11.1 Exception Hierarchy
```
RuntimeException
└── ImageFetchException (parent)
    ├── InvalidUrlException
    ├── TimeoutException
    ├── ImageNotAccessibleException
    └── CrawlingBlockedException
```

### 11.2 Global Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUrl(InvalidUrlException e) {
        // Return 400 Bad Request
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeout(TimeoutException e) {
        // Return 504 Gateway Timeout
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        // Return 500 Internal Server Error
    }
}
```

### 11.3 Fallback Logic
- Fall back to next priority strategy on failure
- Return empty array if all strategies fail (with error logging)

### 11.4 Anti-Crawling Countermeasures
- User-Agent setting: `Mozilla/5.0 (Windows NT 10.0; Win64; x64)...`
- Rate Limiting: Simple delay between requests per domain
- Retry logic: 1 retry on failure (optional for verification)

---

## 12. Testing Strategy

### 12.1 Unit Testing
- **Targets**: Strategy, Service, Util classes
- **Tools**: JUnit 5, Mockito
- **Coverage Goal**: Reasonable coverage, focus on critical paths

**Test Case Examples**:
- DirectUrlImageFetchStrategy
  - ✓ Valid image URL processing
  - ✓ Invalid format URL throws exception
  - ✓ Timeout throws exception
  - ✓ Response within 500ms validation

### 12.2 Integration Testing
- **Targets**: Controller → Service → Strategy full flow
- **Tools**: @SpringBootTest, MockMvc, WireMock (optional for verification)

**Test Scenarios**:
1. Direct image URL only → Return 1 result
2. Sales URL only → Return OG tag image
3. Channel search only → Return top 3 search results
4. All inputs provided → Return max 3 based on priority

### 12.3 Performance Testing
- **Approach**: Manual testing with performance measurement
- **Goals**:
  - Direct URL: Average < 500ms
  - Sales URL: Average < 2000ms
  - Channel Search: Average < 3000ms
- **Method**: Run API calls, measure response times via logs/metrics

### 12.4 Edge Case Testing
- Invalid URL format
- Non-existent image
- Network timeout
- HTML structure change (parsing failure)
- Item page without images
- Crawling blocked (403, 429)

---

## 13. Channel-Specific Implementation Details

### 13.1 Naver Shopping (Priority)
- **Search URL**: `https://search.shopping.naver.com/search/all?query={itemName}+{optionName}`
- **Parsing Targets**:
  - CSS Selector: `.product_list_item img.thumbnail` (top 3)
  - Image URL: `data-src` or `src` attribute
- **Notes**:
  - No JavaScript rendering required (SSR page)
  - Rate Limiting: Max 5 requests per second

### 13.2 G-Market (Future Expansion)
- Search URL pattern analysis
- Check for AJAX response parsing

### 13.3 Coupang (Future Expansion)
- Check login requirement
- Investigate API availability

### 13.4 11st, Auction (Future Expansion)
- Apply similar patterns

---

## 14. Monitoring and Logging

### 14.1 Logging Strategy
- **Request/Response Logging**:
  - Request ID generation (UUID)
  - Request parameters, response time, result count
- **Performance Logging**:
  - Execution time per strategy
  - Total processing time
- **Error Logging**:
  - Stack Trace
  - Request Context information

---

## 15. Development Environment Setup Guide

### 15.1 Prerequisites
- JDK 17 installation
- Gradle 8.x installation (or use Gradle Wrapper)
- IDE: IntelliJ IDEA or Eclipse

### 15.2 Project Initialization
```bash
# Using Spring Initializr
curl https://start.spring.io/starter.zip \
  -d dependencies=web,webflux,validation,lombok \
  -d type=gradle-project \
  -d language=java \
  -d javaVersion=17 \
  -d bootVersion=3.2.5 \
  -d groupId=com.example \
  -d artifactId=item-image-fetch \
  -o item-image-fetch.zip

unzip item-image-fetch.zip
cd item-image-fetch
```

### 15.3 Build and Run
```bash
# Build
./gradlew clean build

# Run
./gradlew bootRun

# Test
./gradlew test
```

### 15.4 API Testing
```bash
# curl example
curl -X POST http://localhost:8080/api/v1/images/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "itemName": "Samsung Galaxy S24",
    "optionName": "Titanium Gray",
    "salesChannel": "NAVER"
  }'
```

---

## 16. Success Criteria (PRD-based)

- ✓ **Image Collection Success Rate**: 80%+ per priority level
- ✓ **Target Response Time Achievement**:
  - Direct URL: < 500ms
  - Sales URL: < 2000ms
  - Channel Search: < 3000ms
- ✓ **Channel Support**: Minimum 3 channels (Naver, G-Market, Coupang)
- ✓ **Reliable Performance Measurement**: Accurate loading time, resolution, file size measurement
- ✓ **Edge Case Handling**: Invalid input, timeout, crawling block countermeasures

---

## 17. Key Risks (Internal Verification)

- **HTML structure change**: Use flexible parsing (regex + CSS Selector)
- **Crawling blocks**: User-Agent setting, rate limiting
- **Network timeouts**: Proper timeout configuration per strategy
- **JavaScript rendering**: Check if SSR available, otherwise skip complex sites
- **Image server issues**: Fallback to next strategy

---

## 18. References

- Spring Boot Official Documentation: https://spring.io/projects/spring-boot
- Jsoup Documentation: https://jsoup.org/
- Spring WebClient: https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html
- Java 17 Features: https://openjdk.org/projects/jdk/17/

---

**Date**: 2025-09-30
**Version**: 1.0
**Author**: Development Team