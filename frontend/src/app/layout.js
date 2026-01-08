import { AuthProvider } from "@/context/AuthProvider";
import "./globals.css";

export const metadata = {
  title: "PlacementPro - Smart Campus Placement System",
  description: "AI-Assisted Smart Campus Placement & Career Management System",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body className="antialiased font-sans">
        <AuthProvider>
          {children}
        </AuthProvider>
      </body>
    </html>
  );
}
