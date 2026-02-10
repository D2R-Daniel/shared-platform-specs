import type { NextAuthConfig } from 'next-auth';
import Credentials from 'next-auth/providers/credentials';
import type { AppConfig } from '../config';

export interface CreateNextAuthConfigOptions {
  /** The app config */
  appConfig: AppConfig;

  /**
   * Async function to verify credentials and return a user object.
   * Return null if credentials are invalid.
   */
  authorize: (credentials: {
    email: string;
    password: string;
  }) => Promise<{
    id: string;
    email: string;
    name: string;
    tenantId?: string;
    roles?: string[];
    activeRole?: string;
    permissions?: string[];
  } | null>;

  /** NextAuth secret (defaults to NEXTAUTH_SECRET env var) */
  secret?: string;

  /** Custom pages configuration */
  pages?: {
    signIn?: string;
    signUp?: string;
    error?: string;
    forgotPassword?: string;
  };

  /** Session max age in seconds (default: 30 days) */
  sessionMaxAge?: number;

  /** Additional NextAuth config to merge */
  extend?: Partial<NextAuthConfig>;
}

/**
 * Creates a NextAuth v5 configuration with credentials provider,
 * JWT strategy, and custom pages.
 *
 * Usage in auth.ts:
 * ```ts
 * import NextAuth from 'next-auth';
 * import { createNextAuthConfig } from '@dream/app/next-auth';
 *
 * const authConfig = createNextAuthConfig({
 *   appConfig,
 *   authorize: async ({ email, password }) => {
 *     const user = await db.user.findUnique({ where: { email } });
 *     if (!user || !verifyPassword(password, user.passwordHash)) return null;
 *     return { id: user.id, email: user.email, name: user.name };
 *   },
 * });
 *
 * export const { handlers, auth, signIn, signOut } = NextAuth(authConfig);
 * ```
 */
export function createNextAuthConfig(
  options: CreateNextAuthConfigOptions,
): NextAuthConfig {
  const {
    authorize,
    secret,
    pages,
    sessionMaxAge = 30 * 24 * 60 * 60, // 30 days
    extend = {},
  } = options;

  const config: NextAuthConfig = {
    secret: secret || process.env.NEXTAUTH_SECRET,

    pages: {
      signIn: pages?.signIn ?? '/auth/login',
      error: pages?.error ?? '/auth/error',
      ...pages,
    },

    session: {
      strategy: 'jwt' as const,
      maxAge: sessionMaxAge,
    },

    providers: [
      Credentials({
        name: 'credentials',
        credentials: {
          email: { label: 'Email', type: 'email' },
          password: { label: 'Password', type: 'password' },
        },
        async authorize(credentials) {
          if (!credentials?.email || !credentials?.password) {
            return null;
          }
          return options.authorize({
            email: credentials.email as string,
            password: credentials.password as string,
          });
        },
      }),
    ],

    callbacks: {
      async jwt({ token, user }) {
        if (user) {
          token.id = user.id;
          token.email = user.email;
          token.name = user.name;

          // Attach platform-specific fields if present
          const platformUser = user as unknown as Record<string, unknown>;
          if (platformUser.tenantId) token.tenantId = platformUser.tenantId;
          if (platformUser.roles) token.roles = platformUser.roles;
          if (platformUser.activeRole) token.activeRole = platformUser.activeRole;
          if (platformUser.permissions) token.permissions = platformUser.permissions;
        }
        return token;
      },

      async session({ session, token }) {
        if (token && session.user) {
          (session.user as unknown as Record<string, unknown>).id = token.id;
          (session.user as unknown as Record<string, unknown>).tenantId = token.tenantId;
          (session.user as unknown as Record<string, unknown>).roles = token.roles;
          (session.user as unknown as Record<string, unknown>).activeRole = token.activeRole;
          (session.user as unknown as Record<string, unknown>).permissions = token.permissions;
        }
        return session;
      },

      ...extend.callbacks,
    },

    ...extend,
  };

  // Merge extend.providers if provided (append to base providers)
  if (extend.providers) {
    config.providers = [...(config.providers ?? []), ...extend.providers];
  }

  return config;
}
