'use client';

import { useState, useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import { ChevronDown, Phone } from 'lucide-react';
import { getCountryCodes, PHONE_RULES, validatePhoneNumber } from '@/utils/phoneUtils';

// Get country codes from utilities
const COUNTRY_CODES = getCountryCodes();

/**
 * Phone Input Component with Country Code Selector
 * Uses Portal for dropdown to escape overflow clipping
 * Premium unified design with country-specific validation
 */
export default function PhoneInput({
    label,
    value,
    onChange,
    error: externalError,
    required = false,
    disabled = false,
    showValidation = true,
    className = ''
}) {
    const [countryCode, setCountryCode] = useState('+91');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [isOpen, setIsOpen] = useState(false);
    const [isFocused, setIsFocused] = useState(false);
    const [validationError, setValidationError] = useState(null);
    
    const dropdownTriggerRef = useRef(null);
    const [dropdownStyles, setDropdownStyles] = useState({});
    
    // Get current country rules
    const currentRule = PHONE_RULES[countryCode] || { digits: 15, placeholder: '1234567890' };
    const maxDigits = currentRule.digits;
    const error = externalError || validationError;

    // Parse initial value & Sanitize
    useEffect(() => {
        if (!value) return;
        
        const sortedCodes = [...COUNTRY_CODES].sort((a, b) => b.code.length - a.code.length);
        const match = sortedCodes.find(c => value.startsWith(c.code));
        
        let newCode = countryCode;
        let newNumber = '';

        if (match) {
            newCode = match.code;
            // STRICT SANITIZATION: Remove all non-digits from the rest
            newNumber = value.slice(match.code.length).replace(/\D/g, '');
        } else {
            // STRICT SANITIZATION: Remove all non-digits from the whole value
            // (Assumes value passed in might be just a number or valid format)
            newNumber = value.replace(/\D/g, '');
        }

        if (countryCode !== newCode) setCountryCode(newCode);
        if (phoneNumber !== newNumber) setPhoneNumber(newNumber);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [value]);

    // Calculate dropdown position
    useEffect(() => {
        if (isOpen && dropdownTriggerRef.current) {
            const rect = dropdownTriggerRef.current.getBoundingClientRect();
            setDropdownStyles({
                position: 'fixed',
                top: `${rect.bottom + 8}px`,
                left: `${rect.left}px`,
                width: '16rem', // w-64
                zIndex: 9999, // Super high z-index
            });
        }
    }, [isOpen]);

    // Click outside handler (modified for Portal)
    useEffect(() => {
        const handleClickOutside = (event) => {
            // Close if click is NOT on the trigger button
            // Note: Clicks inside the portal are handled by valid event bubbling or safe checks
            // But since the portal renders outside the main DOM tree, native event bubbling might be tricky
            // safely relying on the backdrop overlay for closing
            if (dropdownTriggerRef.current && !dropdownTriggerRef.current.contains(event.target)) {
                 // Logic handled by backdrop
            }
        };
    }, []);

    const handleNumberChange = (e) => {
        let val = e.target.value.replace(/\D/g, '');
        
        // Enforce max length based on country
        if (val.length > maxDigits) {
            val = val.slice(0, maxDigits);
        }
        
        setPhoneNumber(val);
        
        // Validate on change if showValidation is enabled
        if (showValidation && val.length > 0) {
            const result = validatePhoneNumber(countryCode, val);
            setValidationError(result.valid ? null : result.error);
        } else {
            setValidationError(null);
        }
        
        onChange({ target: { value: val ? `${countryCode}${val}` : '' } });
    };

    const handleCountrySelect = (code) => {
        setCountryCode(code);
        setIsOpen(false);
        
        // Re-validate with new country rules
        const newRule = PHONE_RULES[code] || { digits: 15 };
        let newNumber = phoneNumber;
        
        // Truncate if number exceeds new country's max digits
        if (newNumber.length > newRule.digits) {
            newNumber = newNumber.slice(0, newRule.digits);
            setPhoneNumber(newNumber);
        }
        
        // Validate with new country
        if (showValidation && newNumber.length > 0) {
            const result = validatePhoneNumber(code, newNumber);
            setValidationError(result.valid ? null : result.error);
        } else {
            setValidationError(null);
        }
        
        if (newNumber) {
            onChange({ target: { value: `${code}${newNumber}` } });
        }
    };

    return (
        <div className={`w-full ${className}`}>
            {label && (
                <label className="block text-sm font-semibold text-gray-700 mb-1.5 ml-1">
                    {label}
                    {required && <span className="text-red-500 ml-1">*</span>}
                </label>
            )}
            
            <div 
                className={`
                    relative flex items-center gap-0
                    w-full h-12
                    bg-white
                    border rounded-xl overflow-hidden
                    transition-all duration-200
                    ${error 
                        ? 'border-red-500 focus-within:ring-4 focus-within:ring-red-500/10' 
                        : isFocused 
                            ? 'border-indigo-600 ring-4 ring-indigo-600/10' 
                            : 'border-gray-200 hover:border-gray-300'
                    }
                    ${disabled ? 'bg-gray-50 opacity-60 cursor-not-allowed' : ''}
                `}
            >
                {/* Left Icon */}
                <div className="pl-3.5 pr-2 flex items-center text-gray-400">
                    <Phone className="h-5 w-5" strokeWidth={1.5} />
                </div>

                {/* Country Code Selector */}
                <button
                    ref={dropdownTriggerRef}
                    type="button"
                    onClick={() => !disabled && setIsOpen(!isOpen)}
                    disabled={disabled}
                    className="flex items-center gap-1.5 pr-2 text-gray-700 font-medium text-sm hover:text-indigo-600 transition-colors focus:outline-none disabled:cursor-not-allowed"
                >
                    <span className="text-lg">{COUNTRY_CODES.find(c => c.code === countryCode)?.flag || '🌐'}</span>
                    <span>{countryCode}</span>
                    <ChevronDown className={`h-3 w-3 text-gray-400 transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`} />
                </button>

                {/* Vertical Divider */}
                <div className="h-6 w-px bg-gray-200 mr-3"></div>

                {/* Number Input */}
                <input
                    type="text"
                    value={phoneNumber}
                    onChange={handleNumberChange}
                    onFocus={() => setIsFocused(true)}
                    onBlur={() => setIsFocused(false)}
                    placeholder={currentRule.placeholder}
                    maxLength={maxDigits}
                    disabled={disabled}
                    className="flex-1 h-full bg-transparent border-none text-gray-900 text-sm font-medium placeholder-gray-400 focus:outline-none focus:ring-0 disabled:cursor-not-allowed disabled:text-gray-500 pr-3"
                />
            </div>

            {/* Portal Dropdown */}
            {isOpen && typeof document !== 'undefined' && createPortal(
                <>
                    {/* Backdrop to handle click outside */}
                    <div 
                        className="fixed inset-0 z-[9998]" 
                        onClick={() => setIsOpen(false)}
                    />
                    
                    {/* Dropdown Menu */}
                    <div 
                        className="fixed bg-white rounded-xl shadow-xl border border-gray-100 py-1 z-[9999] animate-fade-in max-h-64 overflow-y-auto"
                        style={dropdownStyles}
                    >
                        <div className="sticky top-0 bg-gray-50/90 backdrop-blur-sm px-4 py-2 border-b border-gray-100 text-xs font-semibold text-gray-500 tracking-wider">
                            SELECT COUNTRY CODE
                        </div>
                        {COUNTRY_CODES.map((country) => (
                            <button
                                key={country.code + country.country}
                                type="button"
                                onClick={() => handleCountrySelect(country.code)}
                                className={`
                                    w-full px-4 py-2.5 text-left text-sm flex items-center gap-3 transition-colors
                                    ${countryCode === country.code ? 'bg-indigo-50 text-indigo-700 font-medium' : 'text-gray-700 hover:bg-gray-50'}
                                `}
                            >
                                <span className="text-xl leading-none">{country.flag}</span>
                                <span className="w-12">{country.code}</span>
                                <span className="text-gray-500 text-xs truncate">{country.country}</span>
                            </button>
                        ))}
                    </div>
                </>,
                document.body
            )}
            
            {error && (
                <p className="mt-1.5 text-sm text-red-500 flex items-center gap-1 ml-1">
                    <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                    </svg>
                    {error}
                </p>
            )}
        </div>
    );
}
