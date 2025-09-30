/**
 * API Communication Layer
 * Handles all HTTP requests to the backend API
 */

const API_BASE_URL = 'http://localhost:8080';
const API_ENDPOINT = `${API_BASE_URL}/api/v1/images/fetch`;

/**
 * Fetch images from the backend API
 * @param {Object} request - Image fetch request object
 * @param {string} request.itemName - Item name (required)
 * @param {string} [request.optionName] - Option name (optional)
 * @param {string} [request.imageUrl] - Direct image URL (optional)
 * @param {string} [request.salesUrl] - Sales page URL (optional)
 * @param {string} [request.salesChannel] - Sales channel (optional)
 * @returns {Promise<Object>} Image fetch response
 */
async function fetchImages(request) {
    const startTime = Date.now();

    try {
        // Log request for debugging
        console.log('[API] Request:', {
            timestamp: new Date().toISOString(),
            endpoint: API_ENDPOINT,
            payload: request
        });

        // Make API call
        const response = await fetch(API_ENDPOINT, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request)
        });

        const duration = Date.now() - startTime;

        // Handle error responses
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({
                error: 'UNKNOWN_ERROR',
                message: `HTTP ${response.status}: ${response.statusText}`
            }));

            console.error('[API] Error Response:', {
                status: response.status,
                statusText: response.statusText,
                error: errorData,
                duration: `${duration}ms`
            });

            // Throw error with response data
            const error = new Error(errorData.message || 'Failed to fetch images');
            error.status = response.status;
            error.code = errorData.error;
            error.data = errorData;
            throw error;
        }

        // Parse successful response
        const data = await response.json();

        // Log successful response
        console.log('[API] Success Response:', {
            timestamp: new Date().toISOString(),
            status: response.status,
            imageCount: data.images?.length || 0,
            totalLoadingTime: data.totalLoadingTimeMs,
            requestDuration: `${duration}ms`,
            data: data
        });

        return data;

    } catch (error) {
        // Handle network errors
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            console.error('[API] Network Error:', {
                message: 'Failed to connect to backend',
                endpoint: API_ENDPOINT,
                originalError: error.message
            });

            const networkError = new Error('Cannot connect to backend. Is the server running?');
            networkError.status = 0;
            networkError.code = 'NETWORK_ERROR';
            throw networkError;
        }

        // Re-throw other errors
        console.error('[API] Request Failed:', {
            timestamp: new Date().toISOString(),
            error: error.message,
            status: error.status || 'unknown',
            code: error.code || 'unknown'
        });

        throw error;
    }
}

/**
 * Get user-friendly error message based on error status
 * @param {Error} error - Error object
 * @returns {string} User-friendly error message
 */
function getErrorMessage(error) {
    const status = error.status;
    const code = error.code;

    switch (status) {
        case 0:
            return '❌ Cannot connect to server. Please ensure the backend is running on http://localhost:8080';

        case 400:
            if (code === 'INVALID_REQUEST') {
                return `❌ Invalid Request: ${error.message}`;
            }
            return `❌ Bad Request: ${error.message}`;

        case 404:
            return '❌ API endpoint not found. Please check the backend is running correctly.';

        case 500:
            return '❌ Server Error: Something went wrong on the server. Please try again.';

        case 504:
            return '⏱️ Request Timeout: The request took too long. Please try with different parameters.';

        default:
            return `❌ Error: ${error.message || 'An unexpected error occurred'}`;
    }
}

/**
 * Get alert class based on error status
 * @param {number} status - HTTP status code
 * @returns {string} Alert CSS class
 */
function getAlertClass(status) {
    if (status === 0) return 'alert-error';
    if (status === 400) return 'alert-warning';
    if (status === 504) return 'alert-warning';
    return 'alert-error';
}