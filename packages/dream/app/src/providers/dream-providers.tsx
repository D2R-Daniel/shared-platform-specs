'use client';

import React from 'react';
import { SessionProvider } from 'next-auth/react';
import type { AppConfig } from '../config';

export interface DreamProvidersProps {
  children: React.ReactNode;
  config: AppConfig;
  /** Override the base path for NextAuth API routes (default: "/api/auth") */
  basePath?: string;
}

/**
 * Top-level provider wrapper for Dream Platform apps.
 *
 * Wraps the app with:
 * - NextAuth SessionProvider for authentication state
 *
 * Usage in root layout.tsx:
 * ```tsx
 * import { DreamProviders } from '@dream/app/providers';
 * import { appConfig } from './config';
 *
 * export default function RootLayout({ children }) {
 *   return (
 *     <html>
 *       <body>
 *         <DreamProviders config={appConfig}>{children}</DreamProviders>
 *       </body>
 *     </html>
 *   );
 * }
 * ```
 */
export function DreamProviders({
  children,
  config: _config,
  basePath,
}: DreamProvidersProps) {
  return (
    <SessionProvider basePath={basePath}>
      {children}
    </SessionProvider>
  );
}
