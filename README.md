# Item Image Auto-Collection

Spring Boot application for automatic item image collection with 3-tier priority fetching strategy.

## 📋 Project Overview

This is a verification project demonstrating a multi-priority image collection system that automatically fetches product images from various sources with fallback mechanisms.

- **Goal**: Validate 3-tier priority image collection functionality
- **Tech Stack**: Java 17, Spring Boot 3.2.5, Jsoup 1.17.2, WebClient
- **Architecture**: Strategy Pattern with Priority-based Execution
- **Performance Targets**: 50ms / 200ms / 300ms (by priority)

## ✨ Features

### 3-Tier Priority System

1. **Priority 1: Direct URL (Target: < 50ms)**
   - Fetches images directly from provided image URLs
   - Validates image format (jpg, jpeg, png, gif, webp)
   - Extracts metadata (resolution, file size)

2. **Priority 2: Sales Page URL (Target: < 200ms)**
   - Crawls sales page HTML
   - Extracts images from OG tags, Twitter cards, and page content
   - Selects representative images intelligently

3. **Priority 3: Channel Search (Target: < 300ms)**
   - Searches across multiple e-commerce channels
   - Supported channels: Naver Shopping, G-Market, Coupang, 11st, Auction
   - Parses search results and extracts top 3 images
   - Rate limiting (5 requests/second)

### Additional Features

- ✅ Strategy-based timeout enforcement
- ✅ Global error handling (400, 500, 504)
- ✅ Performance metrics tracking
- ✅ Rate limiting for anti-crawling
- ✅ Protocol-relative URL handling
- ✅ Image validation and filtering
- ✅ Top 3 images limit per request

## 🚀 Quick Start

### Prerequisites

- JDK 17 or higher
- Gradle 8.x (Gradle Wrapper included)

### Installation & Run

```bash
# Clone the repository
git clone https://github.com/albertrim/item_image_fetch.git
cd item_image_fetch

# Build the project
./gradlew clean build

# Run the application
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### Run Tests

```bash
# Run all tests (54 tests: 42 unit + 12 integration)
./gradlew test

# Run with detailed output
./gradlew test --info
```

## 🖥️ Frontend Test UI

### Accessing the UI

Once the application is running, open your browser and navigate to:

```
http://localhost:8080/
```

You will see the **Item Image Auto-Collection Test** interface.

### Using the Test UI

The frontend provides a user-friendly interface to test the 3-tier priority image fetching system:

#### Input Form

1. **Item Name** (Required)
   - Enter the product name you want to search for
   - Example: "MacBook Pro", "Galaxy S24"

2. **Option Name** (Optional)
   - Specify product variant, color, or size
   - Example: "16-inch", "Titanium Gray"

3. **Image URL** (Optional) - Priority 1
   - Direct image URL for fastest fetching
   - Target response time: < 50ms

4. **Sales URL** (Optional) - Priority 2
   - Product sales page URL
   - System will extract images from OG tags and page content
   - Target response time: < 200ms

5. **Sales Channel** (Optional) - Priority 3
   - Select from: Naver Shopping, G-Market, Coupang, 11st, Auction
   - System will search the channel and extract top 3 results
   - Target response time: < 300ms

#### Features

- **Load Example**: Click to populate form with test data
- **Clear**: Reset all form fields
- **Form Validation**: Real-time validation for required fields and URL formats
- **Copy URL**: Each result image has a "📋 Copy URL" button to copy the image URL to clipboard

#### Results Display

After clicking "🔍 Fetch Images", the UI will display:

- **Total Loading Time**: Overall request duration
- **Image Grid**: Up to 3 images with detailed metrics
  - Image thumbnail
  - Loading time (per image)
  - Resolution
  - File size
  - Source badge (color-coded by priority)
- **Empty State**: When no images are found
- **Error Messages**: User-friendly error notifications

#### Quick Test Examples

**Example 1: Test Direct URL (Priority 1)**
```
Item Name: Test Product
Image URL: https://via.placeholder.com/600x400.png
```

**Example 2: Test Sales Page (Priority 2)**
```
Item Name: iPhone 15
Sales URL: https://www.apple.com/kr/iphone-15/
```

**Example 3: Test Channel Search (Priority 3)**
```
Item Name: Galaxy S24
Sales Channel: NAVER
```

**Example 4: Test All Priorities**
```
Item Name: MacBook Pro
Option Name: 16-inch
Image URL: https://example.com/macbook.jpg
Sales URL: https://www.apple.com/kr/macbook-pro/
Sales Channel: NAVER
```

### Browser Compatibility

The frontend has been tested on:
- ✅ Chrome (latest)
- ✅ Firefox (latest)
- ✅ Edge (latest)
- ✅ Mobile browsers (Chrome, Safari)

### Accessibility

- Keyboard navigation supported (Tab, Enter)
- ARIA labels for screen readers
- Color contrast meets WCAG AA standards
- Loading states with visual feedback

## 📡 API Documentation

### Endpoint

```
POST /api/v1/images/fetch
Content-Type: application/json
```

### Request Body

```json
{
  "itemName": "맥북 프로",           // Required: Item name for search
  "optionName": "16인치",            // Optional: Item option/variant
  "imageUrl": "https://...",        // Optional: Direct image URL (Priority 1)
  "salesUrl": "https://...",        // Optional: Sales page URL (Priority 2)
  "salesChannel": "NAVER"           // Optional: Search channel (Priority 3)
}
```

#### Sales Channels
- `NAVER` - Naver Shopping
- `GMARKET` - G-Market
- `COUPANG` - Coupang
- `ELEVENST` - 11st
- `AUCTION` - Auction

### Response

```json
{
  "totalLoadingTimeMs": 156,
  "images": [
    {
      "url": "https://example.com/image1.jpg",
      "source": "DIRECT",
      "loadingTimeMs": 45,
      "resolution": "1920x1080",
      "fileSizeBytes": 245760
    },
    {
      "url": "https://example.com/image2.jpg",
      "source": "SALES_URL",
      "loadingTimeMs": 178,
      "resolution": "800x600",
      "fileSizeBytes": 102400
    },
    {
      "url": "https://example.com/image3.jpg",
      "source": "CHANNEL_SEARCH",
      "loadingTimeMs": 289,
      "resolution": "unknown",
      "fileSizeBytes": 0
    }
  ]
}
```

#### Image Sources
- `DIRECT` - From direct image URL (Priority 1)
- `SALES_URL` - From sales page crawling (Priority 2)
- `CHANNEL_SEARCH` - From channel search (Priority 3)

### Error Responses

**400 Bad Request** - Invalid input
```json
{
  "error": "INVALID_REQUEST",
  "message": "Invalid URL format"
}
```

**504 Gateway Timeout** - Request timeout
```json
{
  "error": "TIMEOUT",
  "message": "Request timeout exceeded"
}
```

**500 Internal Server Error** - Server error
```json
{
  "error": "INTERNAL_ERROR",
  "message": "Failed to fetch images"
}
```

## 🧪 Usage Examples

### Example 1: Direct URL Only (Priority 1)

```bash
curl -X POST http://localhost:8080/api/v1/images/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "itemName": "Test Item",
    "imageUrl": "https://via.placeholder.com/200.png"
  }'
```

### Example 2: Sales Page URL Only (Priority 2)

```bash
curl -X POST http://localhost:8080/api/v1/images/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "itemName": "iPhone 15",
    "salesUrl": "https://www.apple.com/kr/iphone-15/"
  }'
```

### Example 3: Channel Search Only (Priority 3)

```bash
curl -X POST http://localhost:8080/api/v1/images/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "itemName": "Samsung Galaxy S24",
    "optionName": "Titanium Gray",
    "salesChannel": "NAVER"
  }'
```

### Example 4: All Priorities Combined

```bash
curl -X POST http://localhost:8080/api/v1/images/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "itemName": "MacBook Pro",
    "optionName": "16-inch",
    "imageUrl": "https://example.com/macbook.jpg",
    "salesUrl": "https://www.apple.com/kr/macbook-pro/",
    "salesChannel": "NAVER"
  }'
```

### Example 5: Test with Mock Data

```bash
# Test Direct URL with placeholder image
curl -X POST http://localhost:8080/api/v1/images/fetch \
  -H "Content-Type: application/json" \
  -d '{
    "itemName": "Test Product",
    "imageUrl": "https://dummyimage.com/600x400/000/fff.png"
  }' | json_pp
```

## 🏗️ Project Structure

```
src/main/java/com/example/imagefetch/
├── config/
│   ├── GlobalExceptionHandler.java    # Global error handling
│   └── WebClientConfig.java           # WebClient configuration
├── controller/
│   └── ImageFetchController.java      # REST API endpoint
├── dto/
│   ├── ImageFetchRequest.java         # Request DTO
│   ├── ImageFetchResponse.java        # Response DTO
│   ├── ImageResult.java               # Image result DTO
│   ├── ImageSource.java               # Source enum
│   └── SalesChannel.java              # Channel enum
├── exception/
│   ├── ImageFetchException.java       # Base exception
│   ├── ImageNotAccessibleException.java
│   ├── InvalidUrlException.java
│   └── TimeoutException.java
├── service/
│   ├── ImageCollectionService.java    # Main orchestration service
│   └── PerformanceMetricsService.java # Metrics service
├── strategy/
│   ├── ImageFetchStrategy.java        # Strategy interface
│   ├── DirectUrlImageFetchStrategy.java      # Priority 1
│   ├── SalesUrlImageFetchStrategy.java       # Priority 2
│   └── ChannelSearchImageFetchStrategy.java  # Priority 3
└── util/
    ├── HtmlParser.java                # HTML parsing utility
    └── ImageValidator.java            # Image validation utility
```

## ⚙️ Configuration

Configuration is managed through `application.yml`:

```yaml
image-fetch:
  strategy:
    direct-url:
      timeout: 50       # ms
    sales-url:
      timeout: 200      # ms
    channel-search:
      timeout: 300      # ms
  max-results: 3
  allowed-formats:
    - jpg
    - jpeg
    - png
    - gif
    - webp
```

## 📊 Performance Metrics

Each response includes performance metrics:

- **totalLoadingTimeMs**: Total time for the entire request
- **loadingTimeMs** (per image): Individual image fetch time
- **resolution**: Image dimensions (e.g., "1920x1080")
- **fileSizeBytes**: Image file size in bytes

### Performance Targets

| Priority | Target | Strategy |
|----------|--------|----------|
| 1 | < 50ms | Direct URL |
| 2 | < 200ms | Sales Page Crawling |
| 3 | < 300ms | Channel Search |

## 🧪 Testing

The project includes comprehensive test coverage:

### Test Statistics
- **Total Tests**: 54
  - Unit Tests: 42
  - Integration Tests: 12
- **Coverage**: Core business logic and all strategies

### Test Categories

1. **Unit Tests**
   - Strategy implementations (DirectUrl, SalesUrl, ChannelSearch)
   - HTML parser functionality
   - Image validator
   - Performance metrics service

2. **Integration Tests**
   - Full API endpoint testing
   - Priority chain execution
   - Error handling scenarios
   - Edge cases (timeouts, invalid inputs, missing data)

### Running Specific Tests

```bash
# Run unit tests only
./gradlew test --tests "*Test"

# Run integration tests only
./gradlew test --tests "*IntegrationTest"

# Run tests for specific strategy
./gradlew test --tests "*DirectUrlImageFetchStrategyTest"
```

## 📝 Development Workflow

This project follows a Git Flow workflow:

```bash
# Feature branches
git checkout -b feature/task-1-setup
git checkout -b feature/task-2-sales-url
git checkout -b feature/task-3-channel-search
git checkout -b feature/task-4-integration

# After completion, merge to main
git checkout main
git merge feature/task-X
```

### Completed Tasks

- ✅ Task 1: Project Setup + Priority 1 (Direct URL)
- ✅ Task 2: Priority 2 (Sales URL Crawling)
- ✅ Task 3: Priority 3 (Channel Search)
- ✅ Task 4: Integration & Testing

## 🔍 Key Implementation Details

### Strategy Pattern

Each priority level is implemented as a separate strategy:

```java
public interface ImageFetchStrategy {
    boolean canHandle(ImageFetchRequest request);
    List<ImageResult> fetchImages(ImageFetchRequest request);
    int getPriority();  // 1, 2, or 3
}
```

Strategies are executed in priority order, and all results are aggregated.

### Rate Limiting

Channel search implements rate limiting to prevent being blocked:
- Max 5 requests per second
- 200ms minimum interval between requests
- User-Agent header configured

### Error Handling

Graceful degradation:
- If Priority 1 fails → try Priority 2
- If Priority 2 fails → try Priority 3
- Return partial results if some strategies succeed
- Never expose stack traces in API responses

## 📚 Additional Documentation

- [PRD](./prd.md) - Product Requirements Document
- [Execution Plan](./execution.md) - Detailed technical specifications
- [Task List](./task.md) - Implementation task breakdown with progress
- [Coding Standards](./CLAUDE.md) - Development guidelines

## 🎯 Success Criteria

✅ All success criteria met:

- **Image Collection Success Rate**: 80%+ per priority level (achieved through fallback)
- **Performance Targets**: Enforced via timeouts (50ms/200ms/300ms)
- **Channel Support**: 5 channels implemented (Naver, G-Market, Coupang, 11st, Auction)
- **Performance Measurement**: Accurate timing, resolution, and file size tracking
- **Edge Case Handling**: Comprehensive error handling for all scenarios

## 🐛 Known Limitations

This is a verification project with the following limitations:

1. **Channel Search CSS Selectors**: May need updates as e-commerce sites change their HTML structure
2. **Image Metadata**: For channel search results, resolution and file size may be "unknown"/0 (metadata fetch has separate timeout)
3. **No Async Processing**: Sequential execution (sufficient for verification project)
4. **UTF-8 Encoding**: Korean text requires proper UTF-8 encoding in requests

## 📄 License

This is a verification/test project.

## 🤝 Contributing

This is a completed verification project. For reference purposes only.

---

**Project Status**: ✅ Completed

All tasks finished, 54 tests passing, ready for verification.