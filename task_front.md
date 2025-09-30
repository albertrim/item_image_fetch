# Item Image Fetch - Frontend Implementation Task List

> For detailed specifications, refer to [prd.md](./prd.md) Section 3 (UI Configuration)

---

## Project Overview
- **Goal**: Build test UI for validating 3-tier priority image collection functionality
- **Duration**: 4 days (4 tasks)
- **Tech Stack**: HTML5, CSS3, Vanilla JavaScript, Fetch API
- **Integration**: Spring Boot static resources
- **Success Criteria**: Functional test UI matching prd.md specifications

---

## Task 5: Frontend Setup + Input Form UI
**Branch**: `feature/task-5-frontend-setup`
**Timeline**: Day 8
**Reference**: prd.md Section 3.1 (Input Screen)

### Git Workflow
```bash
git checkout main && git pull
git checkout -b feature/task-5-frontend-setup
# ... implement ...
git add . && git commit -m "feat: implement frontend setup and input form UI"
git push origin feature/task-5-frontend-setup
# Create PR → main
```

### Implementation Tasks

#### 1. Project Structure Setup
- [ ] Create `src/main/resources/static` directory
- [ ] Create `index.html` - main UI page
- [ ] Create `css/style.css` - stylesheet
- [ ] Create `js/app.js` - main application logic
- [ ] Create `js/api.js` - API communication layer
- [ ] Verify Spring Boot serves static files at `http://localhost:8080/`

#### 2. HTML Structure (Input Form)
- [ ] Create semantic HTML5 structure
- [ ] Add page header: "Item Image Auto-Collection Test"
- [ ] Input field: Item Name (required, text input)
- [ ] Input field: Option Name (optional, text input)
- [ ] Input field: Image URL (optional, URL input)
- [ ] Input field: Sales URL (optional, URL input)
- [ ] Dropdown: Sales Channel (NAVER, GMARKET, COUPANG, ELEVENST, AUCTION)
- [ ] Submit button: "🔍 Fetch Images"
- [ ] Add accessible labels and ARIA attributes

#### 3. CSS Styling (Input Form)
- [ ] Create clean, modern UI matching prd.md mockup
- [ ] Form container with border and padding
- [ ] Input fields with consistent styling
- [ ] Dropdown with custom styling
- [ ] Button with hover and active states
- [ ] Responsive design (mobile-friendly)
- [ ] Add loading state styles (spinner)
- [ ] Color scheme: Professional blue/gray palette

#### 4. JavaScript - Form Handling
- [ ] Initialize form element references
- [ ] Add form submit event listener
- [ ] Prevent default form submission
- [ ] Validate required field (itemName)
- [ ] Validate URL formats (imageUrl, salesUrl)
- [ ] Display validation error messages
- [ ] Collect form data into request object
- [ ] Show loading state on submit

#### 5. JavaScript - API Layer (Stub)
- [ ] Create `api.js` with `fetchImages(request)` function
- [ ] Use Fetch API to call POST `/api/v1/images/fetch`
- [ ] Set Content-Type header to `application/json`
- [ ] Return Promise with response data
- [ ] Add error handling (catch network errors)
- [ ] Log requests to console for debugging

### Validation Checklist
- [ ] Static files served at `http://localhost:8080/`
- [ ] Input form displays correctly
- [ ] Form validation works (required field, URL format)
- [ ] Submit button shows loading state
- [ ] Console logs show form data on submit
- [ ] Responsive design works on mobile viewport

### Deliverable
Functional input form UI with validation, ready for API integration

---

## Task 6: Result Display UI + Image Grid
**Branch**: `feature/task-6-result-display`
**Timeline**: Day 9
**Reference**: prd.md Section 3.2 (Result Screen)

### Git Workflow
```bash
git checkout main && git pull
git checkout -b feature/task-6-result-display
# ... implement ...
git add . && git commit -m "feat: implement result display UI and image grid"
git push origin feature/task-6-result-display
# Create PR → main
```

### Implementation Tasks

#### 1. HTML Structure (Result Section)
- [ ] Create `<section id="results">` container
- [ ] Add "Search Results" header
- [ ] Add total loading time display: "Total Loading Time: Xms"
- [ ] Create 3-column grid container for images
- [ ] Image card template:
  - Image thumbnail (`<img>`)
  - Loading time display
  - Resolution display
  - File size display
  - Source badge (Direct/Sales URL/Channel Search)
- [ ] Add empty state message: "No results. Try fetching images."
- [ ] Add error message container

#### 2. CSS Styling (Result Section)
- [ ] Results container with border and margin
- [ ] Grid layout for 3 images (CSS Grid or Flexbox)
- [ ] Image card styling with shadow and hover effect
- [ ] Image thumbnail sizing (max 300x300px, maintain aspect ratio)
- [ ] Metric text styling (small, gray font)
- [ ] Source badge styling with color coding:
  - DIRECT: Green badge
  - SALES_URL: Blue badge
  - CHANNEL_SEARCH: Orange badge
- [ ] Empty state styling (centered, gray text)
- [ ] Error message styling (red background, white text)
- [ ] Responsive grid (1 column on mobile, 3 on desktop)

#### 3. JavaScript - Result Rendering
- [ ] Create `renderResults(response)` function
- [ ] Display total loading time from response
- [ ] Clear previous results before rendering
- [ ] Loop through response.images (max 3)
- [ ] Create image card elements dynamically
- [ ] Set image src and alt attributes
- [ ] Display loading time: `${loadingTimeMs}ms`
- [ ] Display resolution (handle "unknown" case)
- [ ] Display file size (convert bytes to KB, handle 0)
- [ ] Display source badge with appropriate color
- [ ] Handle empty results (show empty state)

#### 4. JavaScript - Error Handling UI
- [ ] Create `showError(message)` function
- [ ] Display error message in error container
- [ ] Auto-hide error after 5 seconds
- [ ] Create `clearError()` function
- [ ] Style error based on HTTP status:
  - 400: Yellow warning (client error)
  - 500: Red error (server error)
  - 504: Orange warning (timeout)

#### 5. JavaScript - Loading States
- [ ] Show loading spinner on form submit
- [ ] Disable form inputs during loading
- [ ] Hide loading spinner when results/error received
- [ ] Re-enable form inputs after completion
- [ ] Add CSS animation for spinner

### Validation Checklist
- [ ] Result section displays after form submit
- [ ] Image grid shows up to 3 images correctly
- [ ] Performance metrics display accurately
- [ ] Source badges color-coded correctly
- [ ] Empty state shows when no results
- [ ] Error messages display appropriately
- [ ] Loading spinner works during API call
- [ ] Responsive layout works on all screen sizes

### Deliverable
Complete result display UI with image grid and performance metrics

---

## Task 7: API Integration + Error Handling
**Branch**: `feature/task-7-api-integration`
**Timeline**: Day 10
**Reference**: README.md API Documentation

### Git Workflow
```bash
git checkout main && git pull
git checkout -b feature/task-7-api-integration
# ... implement ...
git add . && git commit -m "feat: integrate API and implement error handling"
git push origin feature/task-7-api-integration
# Create PR → main
```

### Implementation Tasks

#### 1. API Integration (api.js)
- [ ] Implement complete `fetchImages(request)` function
- [ ] Build request body with all fields:
  ```javascript
  {
    itemName: string,
    optionName: string | null,
    imageUrl: string | null,
    salesUrl: string | null,
    salesChannel: string | null
  }
  ```
- [ ] Call POST `http://localhost:8080/api/v1/images/fetch`
- [ ] Set headers: `Content-Type: application/json`
- [ ] Handle successful response (200 OK)
- [ ] Parse JSON response body
- [ ] Return ImageFetchResponse object

#### 2. Error Response Handling
- [ ] Check response.ok before parsing
- [ ] Handle 400 Bad Request:
  - Parse error.message
  - Show user-friendly validation error
- [ ] Handle 500 Internal Server Error:
  - Show generic error message
  - Log full error to console
- [ ] Handle 504 Gateway Timeout:
  - Show timeout-specific message
  - Suggest retry
- [ ] Handle network errors (offline):
  - Show connection error message
  - Check if backend is running

#### 3. Request/Response Logging
- [ ] Log request payload to console (DEBUG)
- [ ] Log response data to console (DEBUG)
- [ ] Log errors with stack trace (ERROR)
- [ ] Add timestamp to all logs
- [ ] Create `logger.js` utility (optional)

#### 4. Complete Flow Integration
- [ ] Connect form submit → API call → render results
- [ ] Test with direct image URL (Priority 1)
- [ ] Test with sales URL (Priority 2)
- [ ] Test with channel search (Priority 3)
- [ ] Test with all fields provided (combined)
- [ ] Test with invalid inputs (validation)
- [ ] Test with backend offline (network error)

#### 5. User Experience Enhancements
- [ ] Clear previous results before new search
- [ ] Scroll to results section after fetch
- [ ] Add "Try another search" button to clear form
- [ ] Add example data button for quick testing
- [ ] Add copy-to-clipboard for image URLs
- [ ] Keyboard shortcuts (Enter to submit)

### Validation Checklist
- [ ] API integration works with live backend
- [ ] All 3 priority strategies testable from UI
- [ ] Error messages display correctly for all error types
- [ ] Network error handling works (test with backend off)
- [ ] Console logging helps with debugging
- [ ] User experience smooth and intuitive
- [ ] No console errors during normal operation

### Deliverable
Fully functional frontend integrated with backend API

---

## Task 8: Final Integration Testing + Polish
**Branch**: `feature/task-8-final-polish`
**Timeline**: Day 11
**Reference**: prd.md Section 5 (Test Scenarios)

### Git Workflow
```bash
git checkout main && git pull
git checkout -b feature/task-8-final-polish
# ... implement ...
git add . && git commit -m "feat: final integration testing and UI polish"
git push origin feature/task-8-final-polish
# Create PR → main
```

### Implementation Tasks

#### 1. End-to-End Testing
- [ ] Test Scenario 1: Direct Image URL Only
  - Input: itemName + imageUrl
  - Verify: Result shows 1 image, source=DIRECT, time < 50ms
- [ ] Test Scenario 2: Sales URL Only
  - Input: itemName + salesUrl
  - Verify: Result shows images from sales page, time < 200ms
- [ ] Test Scenario 3: Channel Search Only
  - Input: itemName + salesChannel
  - Verify: Result shows 3 images from channel, time < 300ms
- [ ] Test Scenario 4: All Inputs Combined
  - Input: All fields populated
  - Verify: Result shows max 3 images from priority order
- [ ] Test Scenario 5: Only Item Name (Required)
  - Input: itemName only
  - Verify: Empty result with appropriate message

#### 2. Edge Case Testing
- [ ] Invalid URL format (imageUrl)
  - Verify: Client-side validation error
- [ ] Unreachable URL (imageUrl)
  - Verify: Backend returns empty or error, UI handles gracefully
- [ ] Network timeout scenario
  - Verify: 504 error shown with retry suggestion
- [ ] Backend offline
  - Verify: Network error message displayed
- [ ] Very long item name (>100 chars)
  - Verify: Input handled without breaking UI
- [ ] Special characters in item name (Korean, emoji)
  - Verify: Request encoded properly (UTF-8)
- [ ] Rapid repeated clicks on submit
  - Verify: Request not duplicated, loading state prevents multiple submits

#### 3. Performance Testing
- [ ] Test with real e-commerce URLs (Naver, G-Market, etc.)
- [ ] Verify loading times match targets (50/200/300ms)
- [ ] Test with high-resolution images (>5MB)
  - Verify: UI doesn't freeze, loads properly
- [ ] Test with many concurrent requests
  - Verify: UI remains responsive

#### 4. UI/UX Polish
- [ ] Add favicon to browser tab
- [ ] Add page title: "Item Image Auto-Collection Test"
- [ ] Improve button hover/active animations
- [ ] Add subtle transitions for result rendering
- [ ] Improve spacing and alignment consistency
- [ ] Add tooltips for input fields (explain each field)
- [ ] Add "About" section explaining the tool
- [ ] Add footer with project info and links

#### 5. Accessibility Improvements
- [ ] Verify keyboard navigation works (Tab, Enter, Esc)
- [ ] Add ARIA labels for screen readers
- [ ] Ensure color contrast meets WCAG AA standards
- [ ] Test with screen reader (NVDA or JAWS)
- [ ] Add skip-to-content link
- [ ] Ensure error messages are announced

#### 6. Browser Compatibility
- [ ] Test on Chrome (latest)
- [ ] Test on Firefox (latest)
- [ ] Test on Edge (latest)
- [ ] Test on Safari (if Mac available)
- [ ] Test on mobile Chrome (Android/iOS)
- [ ] Test on mobile Safari (iOS)

#### 7. Code Quality
- [ ] Remove console.log statements (or wrap in DEBUG flag)
- [ ] Add JSDoc comments to functions
- [ ] Organize code with clear sections
- [ ] Extract magic numbers to constants
- [ ] Validate code with ESLint (optional)
- [ ] Minify CSS and JS for production (optional)

#### 8. Documentation Updates
- [ ] Add Frontend section to README.md
- [ ] Document how to access the UI (`http://localhost:8080/`)
- [ ] Add screenshots of UI (input form, results)
- [ ] Document browser requirements
- [ ] Add troubleshooting section for common issues
- [ ] Update Quick Start guide with frontend instructions

### Validation Checklist
- [ ] All test scenarios pass successfully
- [ ] All edge cases handled gracefully
- [ ] Performance targets met (50/200/300ms)
- [ ] UI polish complete (animations, spacing, colors)
- [ ] Accessibility requirements met
- [ ] Cross-browser compatibility verified
- [ ] Code quality standards met
- [ ] Documentation updated with frontend info

### Deliverable
Production-ready frontend test UI with comprehensive testing and documentation

---

## Final Success Criteria (Frontend)
- [ ] **Functional UI**: All input fields and result display working
- [ ] **API Integration**: Successfully calls backend and displays results
- [ ] **Error Handling**: All error scenarios handled gracefully
- [ ] **Performance**: UI responsive, loading times meet targets
- [ ] **Test Coverage**: All scenarios from prd.md Section 5 tested
- [ ] **Accessibility**: WCAG AA compliance for basic accessibility
- [ ] **Browser Support**: Works on Chrome, Firefox, Edge, mobile browsers
- [ ] **Documentation**: Complete usage instructions in README

---

## Quick Reference Links

- **PRD**: [prd.md](./prd.md) - Product Requirements (Section 3: UI)
- **Backend Tasks**: [task.md](./task.md) - Tasks 1-4 (API implementation)
- **API Documentation**: [README.md](./README.md) - API endpoint specs
- **Execution Plan**: [execution.md](./execution.md) - Technical specifications

### Key PRD Sections
- Section 3.1: Input Screen mockup
- Section 3.2: Result Screen mockup
- Section 4: Performance Metrics
- Section 5: Test Scenarios
- Section 6: Functional Requirements

---

## File Structure

```
src/main/resources/static/
├── index.html              # Main UI page
├── css/
│   └── style.css          # Stylesheet
├── js/
│   ├── app.js             # Main application logic
│   └── api.js             # API communication layer
└── assets/                # Optional: images, icons
    └── favicon.ico
```

---

## Example Test Data

### Quick Test - Direct URL
```
Item Name: Test Product
Image URL: https://via.placeholder.com/600x400.png
```

### Quick Test - Sales URL
```
Item Name: MacBook Pro
Sales URL: https://www.apple.com/kr/macbook-pro/
```

### Quick Test - Channel Search
```
Item Name: Galaxy S24
Option Name: Titanium Gray
Sales Channel: NAVER
```

### Quick Test - Combined
```
Item Name: iPhone 15
Option Name: 256GB
Image URL: https://via.placeholder.com/800x600.png
Sales URL: https://www.apple.com/kr/iphone-15/
Sales Channel: NAVER
```

---

**Note**: Each task should be completed, tested, and merged to `main` before starting the next task. The frontend can be developed independently after backend Tasks 1-4 are complete.

---

**Frontend Project Timeline**: Days 8-11 (4 days after backend completion)
**Total Project Duration**: 11 days (7 days backend + 4 days frontend)