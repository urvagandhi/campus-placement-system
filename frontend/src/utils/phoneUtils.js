/**
 * Phone Number Utilities
 * 
 * Provides phone validation rules by country and formatting utilities.
 * Database storage should always use E.164 format: +[country code][number]
 * Example: +919876543210 (India), +14155551234 (US)
 */

/**
 * Country phone rules with validation
 */
export const PHONE_RULES = {
    '+91': { 
        country: 'IN', 
        name: 'India', 
        flag: '🇮🇳', 
        digits: 10, 
        format: (num) => num.replace(/(\d{5})(\d{5})/, '$1 $2'), // 88662 41204
        placeholder: '9876543210'
    },
    '+1': { 
        country: 'US', 
        name: 'USA/Canada', 
        flag: '🇺🇸', 
        digits: 10, 
        format: (num) => num.replace(/(\d{3})(\d{3})(\d{4})/, '($1) $2-$3'), // (415) 555-1234
        placeholder: '4155551234'
    },
    '+44': { 
        country: 'UK', 
        name: 'United Kingdom', 
        flag: '🇬🇧', 
        digits: 10, 
        format: (num) => num.replace(/(\d{4})(\d{6})/, '$1 $2'), // 7911 123456
        placeholder: '7911123456'
    },
    '+61': { 
        country: 'AU', 
        name: 'Australia', 
        flag: '🇦🇺', 
        digits: 9, 
        format: (num) => num.replace(/(\d{3})(\d{3})(\d{3})/, '$1 $2 $3'), // 412 345 678
        placeholder: '412345678'
    },
    '+81': { 
        country: 'JP', 
        name: 'Japan', 
        flag: '🇯🇵', 
        digits: 10, 
        format: (num) => num.replace(/(\d{2})(\d{4})(\d{4})/, '$1-$2-$3'), // 90-1234-5678
        placeholder: '9012345678'
    },
    '+49': { 
        country: 'DE', 
        name: 'Germany', 
        flag: '🇩🇪', 
        digits: 11, 
        format: (num) => num.replace(/(\d{3})(\d{4})(\d{4})/, '$1 $2 $3'), // 151 1234 5678
        placeholder: '15112345678'
    },
    '+33': { 
        country: 'FR', 
        name: 'France', 
        flag: '🇫🇷', 
        digits: 9, 
        format: (num) => num.replace(/(\d{1})(\d{2})(\d{2})(\d{2})(\d{2})/, '$1 $2 $3 $4 $5'), // 6 12 34 56 78
        placeholder: '612345678'
    },
    '+971': { 
        country: 'AE', 
        name: 'UAE', 
        flag: '🇦🇪', 
        digits: 9, 
        format: (num) => num.replace(/(\d{2})(\d{3})(\d{4})/, '$1 $2 $3'), // 50 123 4567
        placeholder: '501234567'
    },
    '+65': { 
        country: 'SG', 
        name: 'Singapore', 
        flag: '🇸🇬', 
        digits: 8, 
        format: (num) => num.replace(/(\d{4})(\d{4})/, '$1 $2'), // 9123 4567
        placeholder: '91234567'
    },
};

/**
 * Get country codes array for dropdown (sorted by usage)
 */
export function getCountryCodes() {
    return Object.entries(PHONE_RULES).map(([code, rule]) => ({
        code,
        country: rule.country,
        name: rule.name,
        flag: rule.flag,
        digits: rule.digits,
    }));
}

/**
 * Validate phone number for a specific country code
 * @param {string} countryCode - E.164 country code (e.g., '+91')
 * @param {string} number - Phone number without country code (digits only)
 * @returns {Object} { valid: boolean, error?: string }
 */
export function validatePhoneNumber(countryCode, number) {
    const rule = PHONE_RULES[countryCode];
    
    if (!rule) {
        return { valid: true }; // Unknown country, allow any
    }
    
    if (!number) {
        return { valid: true }; // Empty is allowed (optional field)
    }
    
    const digits = number.replace(/\D/g, '');
    
    if (digits.length !== rule.digits) {
        return { 
            valid: false, 
            error: `${rule.name} phone numbers must be exactly ${rule.digits} digits` 
        };
    }
    
    return { valid: true };
}

/**
 * Format phone number for display (pretty format)
 * @param {string} e164Phone - Phone in E.164 format (e.g., '+919876543210')
 * @returns {string} Formatted phone (e.g., '+91 98765 43210')
 */
export function formatPhoneForDisplay(e164Phone) {
    if (!e164Phone) return 'N/A';
    
    // Find matching country code (longest first)
    const sortedCodes = Object.keys(PHONE_RULES).sort((a, b) => b.length - a.length);
    const matchedCode = sortedCodes.find(code => e164Phone.startsWith(code));
    
    if (!matchedCode) {
        // Unknown format, just return as-is with minimal formatting
        return e164Phone;
    }
    
    const rule = PHONE_RULES[matchedCode];
    const number = e164Phone.slice(matchedCode.length);
    
    // Apply country-specific formatting
    const formattedNumber = rule.format ? rule.format(number) : number;
    
    return `${matchedCode} ${formattedNumber}`;
}

/**
 * Parse display phone back to E.164
 * @param {string} displayPhone - Formatted phone
 * @returns {string} E.164 format
 */
export function parseToE164(displayPhone) {
    if (!displayPhone) return '';
    return displayPhone.replace(/[\s\-\(\)]/g, '');
}

/**
 * Get the flag emoji for a phone number
 * @param {string} e164Phone - Phone in E.164 format
 * @returns {string} Flag emoji or default
 */
export function getFlagForPhone(e164Phone) {
    if (!e164Phone) return '📞';
    
    const sortedCodes = Object.keys(PHONE_RULES).sort((a, b) => b.length - a.length);
    const matchedCode = sortedCodes.find(code => e164Phone.startsWith(code));
    
    if (!matchedCode) return '📞';
    return PHONE_RULES[matchedCode].flag;
}

const phoneUtils = {
    PHONE_RULES,
    getCountryCodes,
    validatePhoneNumber,
    formatPhoneForDisplay,
    parseToE164,
    getFlagForPhone,
};

export default phoneUtils;
