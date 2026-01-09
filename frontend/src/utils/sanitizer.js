import DOMPurify from 'isomorphic-dompurify';

/**
 * Sanitizes HTML content to prevent XSS attacks.
 * Use this for any content rendered with dangerouslySetInnerHTML.
 *
 * @param {string} dirty - The unsanitized HTML string
 * @returns {string} - The sanitized HTML string
 */
export const sanitizeHtml = (dirty) => {
    return DOMPurify.sanitize(dirty, {
        ALLOWED_TAGS: [
            'p', 'b', 'i', 'em', 'strong', 'a', 'ul', 'ol', 'li', 'br',
            'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'span', 'div'
        ],
        ALLOWED_ATTR: ['href', 'target', 'class', 'id'],
    });
};

/**
 * Strips all HTML tags from a string.
 * Use this for simple text inputs that shouldn't contain any HTML.
 *
 * @param {string} dirty - The unsanitized string
 * @returns {string} - Plain text string
 */
export const stripHtml = (dirty) => {
    return DOMPurify.sanitize(dirty, { ALLOWED_TAGS: [] });
};

export default {
    sanitizeHtml,
    stripHtml
};
