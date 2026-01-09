/**
 * Enhanced fetch utility with retry logic and exponential backoff
 */

const DEFAULT_RETRIES = 3;
const INITIAL_BACKOFF_MS = 1000;

/**
 * Sleeps for a given number of milliseconds
 */
const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

/**
 * Enhanced fetch with retry capability
 *
 * @param {string} url - Target URL
 * @param {Object} options - Fetch options
 * @param {number} retries - Max number of retries
 * @returns {Promise<Response>}
 */
export async function fetchWithRetry(url, options = {}, retries = DEFAULT_RETRIES) {
    let lastError;

    for (let i = 0; i < retries; i++) {
        try {
            const response = await fetch(url, options);

            // If response is successful (2xx), return it
            if (response.ok) {
                return response;
            }

            // Only retry on 5xx errors or 429 Too Many Requests
            if (response.status < 500 && response.status !== 429) {
                return response;
            }

            lastError = new Error(`HTTP ${response.status}: ${response.statusText}`);
        } catch (error) {
            lastError = error;

            // Abort retrying if it's a network error that won't resolve (e.g. invalid URL)
            if (error.name === 'TypeError' && !navigator.onLine) {
                throw error;
            }
        }

        // Don't sleep after the last retry
        if (i < retries - 1) {
            // Exponential backoff: 1s, 2s, 4s...
            const backoff = INITIAL_BACKOFF_MS * Math.pow(2, i);
            console.warn(`Request failed to ${url}. Retrying in ${backoff}ms... (Attempt ${i + 1}/${retries})`);
            await sleep(backoff);
        }
    }

    throw lastError;
}

export default fetchWithRetry;
