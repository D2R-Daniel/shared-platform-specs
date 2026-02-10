import { type NextRequest, NextResponse } from 'next/server';

export interface AuthMiddlewareOptions {
  /**
   * Routes that do not require authentication.
   * Supports exact paths ("/about") and prefix patterns ("/auth/*").
   * Default includes: /auth/*, /api/auth/*, /_next/*, /favicon.ico
   */
  publicRoutes?: string[];

  /** The login page URL to redirect unauthenticated users to (default: "/auth/login") */
  loginUrl?: string;

  /** Cookie name used by NextAuth for the session token */
  sessionCookieName?: string;
}

function isPublicRoute(pathname: string, publicRoutes: string[]): boolean {
  return publicRoutes.some((route) => {
    if (route.endsWith('/*')) {
      const prefix = route.slice(0, -2);
      return pathname === prefix || pathname.startsWith(prefix + '/');
    }
    return pathname === route;
  });
}

const DEFAULT_PUBLIC_ROUTES = [
  '/auth/*',
  '/api/auth/*',
  '/_next/*',
  '/favicon.ico',
];

/**
 * Creates a Next.js middleware function that protects routes.
 *
 * Checks for the NextAuth session cookie and redirects unauthenticated
 * users to the login page, preserving the original URL as callbackUrl.
 *
 * Usage in middleware.ts:
 * ```ts
 * import { createAuthMiddleware } from '@dream/app/next-auth';
 *
 * export default createAuthMiddleware({
 *   publicRoutes: ['/auth/*', '/api/auth/*', '/about'],
 *   loginUrl: '/auth/login',
 * });
 *
 * export const config = {
 *   matcher: ['/((?!_next/static|_next/image|favicon.ico).*)'],
 * };
 * ```
 */
export function createAuthMiddleware(options: AuthMiddlewareOptions = {}) {
  const {
    publicRoutes = [],
    loginUrl = '/auth/login',
    sessionCookieName,
  } = options;

  const allPublicRoutes = [...DEFAULT_PUBLIC_ROUTES, ...publicRoutes];

  return function middleware(request: NextRequest): NextResponse {
    const { pathname } = request.nextUrl;

    // Allow public routes
    if (isPublicRoute(pathname, allPublicRoutes)) {
      return NextResponse.next();
    }

    // Allow static assets
    if (
      pathname.startsWith('/_next/') ||
      pathname.includes('.') // files with extensions (css, js, images, etc.)
    ) {
      return NextResponse.next();
    }

    // Check for session cookie
    const cookieName =
      sessionCookieName ??
      (process.env.NODE_ENV === 'production'
        ? '__Secure-authjs.session-token'
        : 'authjs.session-token');

    const sessionToken = request.cookies.get(cookieName);

    if (!sessionToken) {
      const url = request.nextUrl.clone();
      url.pathname = loginUrl;
      url.searchParams.set('callbackUrl', pathname);
      return NextResponse.redirect(url);
    }

    return NextResponse.next();
  };
}
