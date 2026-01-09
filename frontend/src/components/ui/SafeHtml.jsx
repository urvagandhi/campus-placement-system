'use client';

import { sanitizeHtml } from '@/utils/sanitizer';

/**
 * A safe alternative to dangerouslySetInnerHTML.
 * Encapsulates the sanitization logic.
 */
export default function SafeHtml({ html, className = '' }) {
    if (!html) return null;

    const sanitized = sanitizeHtml(html);

    return (
        <div
            className={`prose max-w-none ${className}`}
            dangerouslySetInnerHTML={{ __html: sanitized }}
        />
    );
}
