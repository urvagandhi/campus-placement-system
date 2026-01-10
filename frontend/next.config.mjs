/** @type {import('next').NextConfig} */
const nextConfig = {
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'Content-Security-Policy',
            value: "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; img-src 'self' data: https://*.tile.openstreetmap.org https://*.tile.osm.org; font-src 'self' https://fonts.gstatic.com data:; connect-src 'self' http://localhost:8080 http://127.0.0.1:8080 http://localhost:3000 ws://localhost:3000; frame-ancestors 'none';",
          },
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'Strict-Transport-Security',
            value: 'max-age=31536000; includeSubDomains',
          },
          {
            key: 'Referrer-Policy',
            value: 'strict-origin-when-cross-origin',
          },
        ],
      },
    ];
  },
  // Note: experimental.sri removed - not supported by Turbopack in Next.js 16
  // Re-add when Turbopack supports SRI: experimental: { sri: { algorithm: 'sha256' } }
};

export default nextConfig;
