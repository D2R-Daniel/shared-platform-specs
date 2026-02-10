import NextAuth from 'next-auth';
import type { NextAuthConfig } from 'next-auth';

/**
 * Creates the NextAuth route handler for App Router.
 *
 * Usage in app/api/auth/[...nextauth]/route.ts:
 * ```ts
 * import { createAuthRouteHandler } from '@dream/app/next-auth';
 * import { authConfig } from '@/auth';
 *
 * const { GET, POST } = createAuthRouteHandler(authConfig);
 * export { GET, POST };
 * ```
 */
export function createAuthRouteHandler(config: NextAuthConfig): {
  GET: (...args: unknown[]) => Promise<Response>;
  POST: (...args: unknown[]) => Promise<Response>;
} {
  const { handlers } = NextAuth(config);
  return {
    GET: handlers.GET as (...args: unknown[]) => Promise<Response>,
    POST: handlers.POST as (...args: unknown[]) => Promise<Response>,
  };
}
