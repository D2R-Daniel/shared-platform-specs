'use client';

import React, { useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { AuthLayout } from '@dream/ui/auth';
import { SignupForm } from '@dream/ui/auth';
import type { ComponentType, ReactNode } from 'react';

export interface SignupPageProps {
  /** URL to redirect to after successful signup (default: "/auth/login") */
  redirectTo?: string;
  /** Custom logo component */
  logo?: ComponentType<{ className?: string }>;
  /** Additional content below the form */
  footer?: ReactNode;
  /** Page title */
  title?: string;
  /** Page description */
  description?: string;
  /** API endpoint for signup (default: "/api/auth/signup") */
  signupEndpoint?: string;
}

/**
 * Full signup page combining AuthLayout + SignupForm from @dream/ui.
 *
 * Handles form submission via API call, error display, and redirect.
 *
 * Usage in app/auth/signup/page.tsx:
 * ```tsx
 * import { SignupPage } from '@dream/app/auth';
 * export default function Page() {
 *   return <SignupPage redirectTo="/auth/login" />;
 * }
 * ```
 */
export function SignupPage({
  redirectTo,
  logo: Logo,
  footer,
  title = 'Create an account',
  description = 'Enter your details to get started',
  signupEndpoint = '/api/auth/signup',
}: SignupPageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [error, setError] = useState<string | null>(null);

  const callbackUrl = redirectTo || searchParams.get('callbackUrl') || '/auth/login';
  const invitationToken = searchParams.get('token') || undefined;

  async function handleSuccess() {
    setError(null);
    router.push(callbackUrl);
  }

  function handleError(err: Error) {
    setError(err.message);
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

      <SignupForm
        onSuccess={handleSuccess}
        onError={handleError}
        invitationToken={invitationToken}
        slots={{
          footer: footer ? <div className="mt-4">{footer}</div> : undefined,
        }}
      />
    </AuthLayout>
  );
}
