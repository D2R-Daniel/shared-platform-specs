'use client';

import React, { useState } from 'react';
import { signIn } from 'next-auth/react';
import { useRouter, useSearchParams } from 'next/navigation';
import { AuthLayout } from '@dream/ui/auth';
import { LoginForm } from '@dream/ui/auth';
import type { ComponentType, ReactNode } from 'react';

export interface LoginPageProps {
  /** URL to redirect to after successful login (default: "/") */
  redirectTo?: string;
  /** Show social login buttons */
  showSocialLogin?: boolean;
  /** Social providers to show (default: ['credentials']) */
  providers?: Array<'credentials' | 'google' | 'azure-entra' | 'generic-oidc'>;
  /** Custom logo component to render above the form */
  logo?: ComponentType<{ className?: string }>;
  /** Additional content below the form */
  footer?: ReactNode;
  /** Auth page title */
  title?: string;
  /** Auth page description */
  description?: string;
}

/**
 * Full login page combining AuthLayout + LoginForm from @dream/ui.
 *
 * Handles form submission via next-auth signIn(), error display,
 * and redirect on success.
 *
 * Usage in app/auth/login/page.tsx:
 * ```tsx
 * import { LoginPage } from '@dream/app/auth';
 * export default function Page() {
 *   return <LoginPage redirectTo="/dashboard" />;
 * }
 * ```
 */
export function LoginPage({
  redirectTo,
  showSocialLogin = false,
  providers,
  logo: Logo,
  footer,
  title = 'Sign in',
  description = 'Enter your credentials to access your account',
}: LoginPageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [error, setError] = useState<string | null>(null);

  const callbackUrl = redirectTo || searchParams.get('callbackUrl') || '/';
  const authProviders = providers ?? (showSocialLogin ? ['credentials', 'google'] : ['credentials']);

  async function handleSuccess(data: { email: string; password: string }) {
    setError(null);
    try {
      const result = await signIn('credentials', {
        email: data.email,
        password: data.password,
        redirect: false,
        callbackUrl,
      });

      if (result?.error) {
        setError('Invalid email or password. Please try again.');
        return;
      }

      router.push(callbackUrl);
      router.refresh();
    } catch {
      setError('An unexpected error occurred. Please try again.');
    }
  }

  return (
    <AuthLayout title={title} description={description}>
      {Logo && (
        <div className="flex justify-center mb-4">
          <Logo className="h-10" />
        </div>
      )}

      {error && (
        <div className="rounded-md bg-destructive/15 p-3 text-sm text-destructive mb-4">
          {error}
        </div>
      )}

      <LoginForm
        providers={authProviders}
        onSuccess={handleSuccess}
        callbackUrl={callbackUrl}
        slots={{
          footer: footer ? <div className="mt-4">{footer}</div> : undefined,
        }}
      />
    </AuthLayout>
  );
}
