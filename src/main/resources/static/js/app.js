/**
 * Main Application Logic
 * Handles form submission, validation, and UI updates
 */

// ============================================
// DOM Elements
// ============================================
const form = document.getElementById('imageForm');
const submitBtn = document.getElementById('submitBtn');
const clearBtn = document.getElementById('clearBtn');
const exampleBtn = document.getElementById('exampleBtn');
const loadingSpinner = document.getElementById('loadingSpinner');
const globalError = document.getElementById('globalError');
const resultsSection = document.getElementById('resultsSection');
const imageGrid = document.getElementById('imageGrid');
const emptyState = document.getElementById('emptyState');
const totalLoadingTimeEl = document.getElementById('totalLoadingTime');

// Form inputs
const itemNameInput = document.getElementById('itemName');
const optionNameInput = document.getElementById('optionName');
const imageUrlInput = document.getElementById('imageUrl');
const salesUrlInput = document.getElementById('salesUrl');
const salesChannelSelect = document.getElementById('salesChannel');

// Error message elements
const itemNameError = document.getElementById('itemNameError');
const imageUrlError = document.getElementById('imageUrlError');
const salesUrlError = document.getElementById('salesUrlError');

// ============================================
// Event Listeners
// ============================================
document.addEventListener('DOMContentLoaded', () => {
    console.log('[App] Application initialized');

    // Form submission
    form.addEventListener('submit', handleFormSubmit);

    // Clear button
    clearBtn.addEventListener('click', handleClearForm);

    // Example button
    exampleBtn.addEventListener('click', handleLoadExample);

    // Real-time validation
    itemNameInput.addEventListener('blur', () => validateItemName());
    imageUrlInput.addEventListener('blur', () => validateUrl(imageUrlInput, imageUrlError));
    salesUrlInput.addEventListener('blur', () => validateUrl(salesUrlInput, salesUrlError));
});

// ============================================
// Form Handling
// ============================================

/**
 * Handle form submission
 * @param {Event} event - Form submit event
 */
async function handleFormSubmit(event) {
    event.preventDefault();

    console.log('[App] Form submitted');

    // Clear previous errors
    clearErrors();

    // Validate form
    if (!validateForm()) {
        console.log('[App] Form validation failed');
        return;
    }

    // Collect form data
    const request = collectFormData();
    console.log('[App] Form data collected:', request);

    // Show loading state
    showLoading();

    try {
        // Call API
        const response = await fetchImages(request);

        // Hide loading
        hideLoading();

        // Render results
        renderResults(response);

        // Scroll to results
        scrollToResults();

    } catch (error) {
        // Hide loading
        hideLoading();

        // Show error message
        showGlobalError(error);
    }
}

/**
 * Validate form inputs
 * @returns {boolean} True if form is valid
 */
function validateForm() {
    let isValid = true;

    // Validate item name (required)
    if (!validateItemName()) {
        isValid = false;
    }

    // Validate URLs (optional, but must be valid if provided)
    if (imageUrlInput.value && !validateUrl(imageUrlInput, imageUrlError)) {
        isValid = false;
    }

    if (salesUrlInput.value && !validateUrl(salesUrlInput, salesUrlError)) {
        isValid = false;
    }

    return isValid;
}

/**
 * Validate item name field
 * @returns {boolean} True if valid
 */
function validateItemName() {
    const value = itemNameInput.value.trim();

    if (!value) {
        showFieldError(itemNameError, 'Item name is required');
        return false;
    }

    clearFieldError(itemNameError);
    return true;
}

/**
 * Validate URL field
 * @param {HTMLInputElement} input - Input element
 * @param {HTMLElement} errorElement - Error message element
 * @returns {boolean} True if valid
 */
function validateUrl(input, errorElement) {
    const value = input.value.trim();

    // Empty is valid (optional field)
    if (!value) {
        clearFieldError(errorElement);
        return true;
    }

    // Check URL format
    try {
        new URL(value);
        clearFieldError(errorElement);
        return true;
    } catch (e) {
        showFieldError(errorElement, 'Invalid URL format. Must start with http:// or https://');
        return false;
    }
}

/**
 * Show field validation error
 * @param {HTMLElement} errorElement - Error message element
 * @param {string} message - Error message
 */
function showFieldError(errorElement, message) {
    errorElement.textContent = message;
    errorElement.removeAttribute('hidden');
}

/**
 * Clear field validation error
 * @param {HTMLElement} errorElement - Error message element
 */
function clearFieldError(errorElement) {
    errorElement.textContent = '';
    errorElement.setAttribute('hidden', '');
}

/**
 * Clear all error messages
 */
function clearErrors() {
    clearFieldError(itemNameError);
    clearFieldError(imageUrlError);
    clearFieldError(salesUrlError);
    hideGlobalError();
}

/**
 * Collect form data into request object
 * @returns {Object} Request object
 */
function collectFormData() {
    return {
        itemName: itemNameInput.value.trim(),
        optionName: optionNameInput.value.trim() || null,
        imageUrl: imageUrlInput.value.trim() || null,
        salesUrl: salesUrlInput.value.trim() || null,
        salesChannel: salesChannelSelect.value || null
    };
}

/**
 * Clear form inputs
 */
function handleClearForm() {
    form.reset();
    clearErrors();
    hideResults();
    console.log('[App] Form cleared');
}

/**
 * Load example data into form
 */
function handleLoadExample() {
    itemNameInput.value = 'MacBook Pro';
    optionNameInput.value = '16-inch';
    imageUrlInput.value = 'https://via.placeholder.com/600x400.png';
    salesUrlInput.value = 'https://www.apple.com/kr/macbook-pro/';
    salesChannelSelect.value = 'NAVER';

    clearErrors();
    console.log('[App] Example data loaded');
}

// ============================================
// UI State Management
// ============================================

/**
 * Show loading spinner and disable form
 */
function showLoading() {
    loadingSpinner.removeAttribute('hidden');
    submitBtn.disabled = true;
    clearBtn.disabled = true;
    exampleBtn.disabled = true;

    // Disable all inputs
    Array.from(form.elements).forEach(element => {
        if (element.tagName === 'INPUT' || element.tagName === 'SELECT') {
            element.disabled = true;
        }
    });
}

/**
 * Hide loading spinner and enable form
 */
function hideLoading() {
    loadingSpinner.setAttribute('hidden', '');
    submitBtn.disabled = false;
    clearBtn.disabled = false;
    exampleBtn.disabled = false;

    // Enable all inputs
    Array.from(form.elements).forEach(element => {
        if (element.tagName === 'INPUT' || element.tagName === 'SELECT') {
            element.disabled = false;
        }
    });
}

/**
 * Show global error message
 * @param {Error} error - Error object
 */
function showGlobalError(error) {
    const message = getErrorMessage(error);
    const alertClass = getAlertClass(error.status || 0);

    globalError.textContent = message;
    globalError.className = `alert ${alertClass}`;
    globalError.removeAttribute('hidden');

    // Auto-hide after 10 seconds
    setTimeout(() => {
        hideGlobalError();
    }, 10000);
}

/**
 * Hide global error message
 */
function hideGlobalError() {
    globalError.setAttribute('hidden', '');
}

/**
 * Show results section
 */
function showResults() {
    resultsSection.removeAttribute('hidden');
}

/**
 * Hide results section
 */
function hideResults() {
    resultsSection.setAttribute('hidden', '');
}

/**
 * Scroll to results section
 */
function scrollToResults() {
    resultsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// ============================================
// Results Rendering
// ============================================

/**
 * Render API response results
 * @param {Object} response - API response object
 */
function renderResults(response) {
    console.log('[App] Rendering results:', response);

    // Show results section
    showResults();

    // Display total loading time
    totalLoadingTimeEl.textContent = response.totalLoadingTimeMs || 0;

    // Clear previous results
    imageGrid.innerHTML = '';

    // Check if there are images
    if (!response.images || response.images.length === 0) {
        emptyState.removeAttribute('hidden');
        console.log('[App] No images in response');
        return;
    }

    // Hide empty state
    emptyState.setAttribute('hidden', '');

    // Render each image
    response.images.forEach((image, index) => {
        const card = createImageCard(image, index);
        imageGrid.appendChild(card);
    });

    console.log('[App] Rendered', response.images.length, 'images');
}

/**
 * Create image card element
 * @param {Object} image - Image result object
 * @param {number} index - Image index
 * @returns {HTMLElement} Image card element
 */
function createImageCard(image, index) {
    const card = document.createElement('div');
    card.className = 'image-card';
    card.setAttribute('role', 'article');
    card.setAttribute('aria-label', `Image ${index + 1}`);

    // Image thumbnail
    const img = document.createElement('img');
    img.src = image.url;
    img.alt = `Result image ${index + 1}`;
    img.className = 'image-thumbnail';
    img.loading = 'lazy';
    img.onerror = () => {
        img.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="250"%3E%3Crect fill="%23f1f5f9" width="300" height="250"/%3E%3Ctext fill="%2394a3b8" font-family="sans-serif" font-size="18" x="50%25" y="50%25" text-anchor="middle" dominant-baseline="middle"%3EImage not available%3C/text%3E%3C/svg%3E';
    };

    // Image info container
    const infoDiv = document.createElement('div');
    infoDiv.className = 'image-info';

    // Loading time
    const loadingTimeMetric = createMetricElement('Loading Time', `${image.loadingTimeMs}ms`);

    // Resolution
    const resolution = image.resolution || 'unknown';
    const resolutionMetric = createMetricElement('Resolution', resolution);

    // File size
    const fileSizeKB = image.fileSizeBytes > 0
        ? Math.round(image.fileSizeBytes / 1024)
        : 0;
    const fileSizeText = fileSizeKB > 0 ? `${fileSizeKB} KB` : 'unknown';
    const fileSizeMetric = createMetricElement('File Size', fileSizeText);

    // Source badge
    const sourceBadge = createSourceBadge(image.source);

    // Copy URL button
    const copyButton = createCopyButton(image.url);

    // Append elements
    infoDiv.appendChild(loadingTimeMetric);
    infoDiv.appendChild(resolutionMetric);
    infoDiv.appendChild(fileSizeMetric);
    infoDiv.appendChild(sourceBadge);
    infoDiv.appendChild(copyButton);

    card.appendChild(img);
    card.appendChild(infoDiv);

    return card;
}

/**
 * Create metric element
 * @param {string} label - Metric label
 * @param {string} value - Metric value
 * @returns {HTMLElement} Metric element
 */
function createMetricElement(label, value) {
    const metricDiv = document.createElement('div');
    metricDiv.className = 'image-metric';

    const labelSpan = document.createElement('span');
    labelSpan.className = 'metric-label';
    labelSpan.textContent = label + ':';

    const valueSpan = document.createElement('span');
    valueSpan.className = 'metric-value-sm';
    valueSpan.textContent = value;

    metricDiv.appendChild(labelSpan);
    metricDiv.appendChild(valueSpan);

    return metricDiv;
}

/**
 * Create source badge element
 * @param {string} source - Image source (DIRECT, SALES_URL, CHANNEL_SEARCH)
 * @returns {HTMLElement} Badge element
 */
function createSourceBadge(source) {
    const badge = document.createElement('span');
    badge.className = 'source-badge';

    switch (source) {
        case 'DIRECT':
            badge.classList.add('source-direct');
            badge.textContent = 'Direct URL';
            break;
        case 'SALES_URL':
            badge.classList.add('source-sales-url');
            badge.textContent = 'Sales Page';
            break;
        case 'CHANNEL_SEARCH':
            badge.classList.add('source-channel-search');
            badge.textContent = 'Channel Search';
            break;
        default:
            badge.textContent = source;
    }

    return badge;
}

/**
 * Create copy URL button
 * @param {string} url - Image URL to copy
 * @returns {HTMLElement} Copy button element
 */
function createCopyButton(url) {
    const button = document.createElement('button');
    button.className = 'copy-url-btn';
    button.textContent = '📋 Copy URL';
    button.setAttribute('aria-label', 'Copy image URL to clipboard');
    button.setAttribute('title', 'Copy image URL');

    button.addEventListener('click', async () => {
        try {
            await navigator.clipboard.writeText(url);

            // Visual feedback
            const originalText = button.textContent;
            button.textContent = '✓ Copied!';
            button.classList.add('copied');

            // Reset after 2 seconds
            setTimeout(() => {
                button.textContent = originalText;
                button.classList.remove('copied');
            }, 2000);

            console.log('[App] URL copied to clipboard:', url);
        } catch (error) {
            console.error('[App] Failed to copy URL:', error);
            button.textContent = '✗ Failed';
            setTimeout(() => {
                button.textContent = '📋 Copy URL';
            }, 2000);
        }
    });

    return button;
}

// ============================================
// Initialization
// ============================================
console.log('[App] Script loaded successfully');