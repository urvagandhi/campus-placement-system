/**
 * Global Password Policy
 * 
 * Centralized password validation rules used across the application.
 * Keep in sync with backend validation in PasswordResetController.java
 * 
 * Requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one digit
 * - At least one special character
 */

export const PASSWORD_RULES = {
    minLength: 8,
    requireUppercase: true,
    requireLowercase: true,
    requireDigit: true,
    requireSpecial: true,
};

/**
 * Validates a password against all rules
 * @param {string} password - Password to validate
 * @returns {Object} Validation result with individual rule checks
 */
export function validatePassword(password) {
    const validations = {
        minLength: password.length >= PASSWORD_RULES.minLength,
        hasUppercase: /[A-Z]/.test(password),
        hasLowercase: /[a-z]/.test(password),
        hasNumber: /[0-9]/.test(password),
        hasSpecial: /[!@#$%^&*(),.?":{}|<>_\-+=\[\]\\;'/`~]/.test(password),
    };

    const isValid = Object.values(validations).every(v => v);

    return {
        ...validations,
        isValid,
    };
}

/**
 * Check if two passwords match
 * @param {string} password - Password
 * @param {string} confirmPassword - Confirmation password
 * @returns {boolean} Whether passwords match
 */
export function passwordsMatch(password, confirmPassword) {
    return password.length > 0 && password === confirmPassword;
}

/**
 * Get human-readable password requirements for display
 * @returns {Array} Array of requirement objects with key and text
 */
export function getPasswordRequirements() {
    return [
        { key: 'minLength', text: `${PASSWORD_RULES.minLength}+ characters` },
        { key: 'hasUppercase', text: 'Uppercase letter' },
        { key: 'hasLowercase', text: 'Lowercase letter' },
        { key: 'hasNumber', text: 'Number' },
        { key: 'hasSpecial', text: 'Special character (!@#$%...)' },
    ];
}

const passwordPolicy = {
    PASSWORD_RULES,
    validatePassword,
    passwordsMatch,
    getPasswordRequirements,
};

export default passwordPolicy;
