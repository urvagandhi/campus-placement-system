const nextJest = require('next/jest');

const createJestConfig = nextJest({
    // Provide the path to your Next.js app to load next.config.js and .env files
    dir: './',
});

// Add any custom config to be passed to Jest
const customJestConfig = {
    // Setup files to run after Jest environment is set up
    setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],

    // Test environment
    testEnvironment: 'jest-environment-jsdom',

    // Module name mapping for path aliases
    moduleNameMapper: {
        '^@/(.*)$': '<rootDir>/src/$1',
    },

    // Test file patterns
    testMatch: [
        '**/__tests__/**/*.[jt]s?(x)',
        '**/?(*.)+(spec|test).[jt]s?(x)',
    ],

    // Ignore Playwright e2e tests - they run separately
    testPathIgnorePatterns: [
        '/node_modules/',
        '/e2e/',
    ],

    // Coverage configuration
    collectCoverageFrom: [
        'src/**/*.{js,jsx}',
        '!src/**/*.d.ts',
        '!src/app/layout.js',
        '!src/app/**/layout.jsx',
    ],

    // Transform ignore patterns
    transformIgnorePatterns: [
        '/node_modules/',
        '^.+\\.module\\.(css|sass|scss)$',
    ],

    // Module file extensions
    moduleFileExtensions: ['js', 'jsx', 'ts', 'tsx', 'json'],
};

// createJestConfig is exported this way to ensure that next/jest can load the Next.js config which is async
module.exports = createJestConfig(customJestConfig);
