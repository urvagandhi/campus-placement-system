import { AuthProvider } from "@/context/AuthProvider";
import "./globals.css";
import { Toaster } from 'react-hot-toast';

export const metadata = {
  title: "PlacementPro - Smart Campus Placement System",
  description: "AI-Assisted Smart Campus Placement & Career Management System",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body className="antialiased font-sans">
        <AuthProvider>
          <Toaster
            position="top-center"
            reverseOrder={false}
            gutter={10}
            toastOptions={{
              className: '',
              duration: 4000,
              style: {
                background: '#ffffff',
                color: '#09090b', // zinc-950
                border: '1px solid #e4e4e7', // zinc-200
                boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08), 0 2px 4px rgba(0,0,0,0.06)',
                borderRadius: '8px', 
                padding: '12px 16px',
                fontSize: '0.875rem',
                fontWeight: 500,
                maxWidth: '356px',
                fontFamily: 'inherit',
              },
              success: {
                style: {
                  borderColor: '#bbf7d0', // green-200
                  background: '#f0fdf4', // green-50
                  color: '#15803d', // green-700
                },
                iconTheme: {
                  primary: '#22c55e', // green-500
                  secondary: '#ffffff',
                },
              },
              error: {
                style: {
                  borderColor: '#fecaca', // red-200
                  background: '#fef2f2', // red-50
                  color: '#b91c1c', // red-700
                },
                iconTheme: {
                  primary: '#ef4444', // red-500
                  secondary: '#ffffff',
                },
              },
            }}
          />
          {children}
        </AuthProvider>
      </body>
    </html>
  );
}
