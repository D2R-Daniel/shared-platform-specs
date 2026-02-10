'use client';

import React, { useState } from 'react';
import { AuthLayout } from '@dream/ui/auth';
import { ForgotPasswordForm } from '@dream/ui/auth';
import type { ComponentType, ReactNode } from 'react';

export interface ForgotPasswordPageProps {
  /** Custom logo component */
  logo?: ComponentType<{ className?: string }>;
  /** Additional content below the form */
  footer?: ReactNode;
  /** Page title */
  title?: string;
  /** Page description */
  description?: string;
}

/**
 * Forgot password page combining AuthLayout + ForgotPasswordForm from @dream/ui.
 *
 * Usage in app/auth/forgot-password/page.tsx:
 * ```tsx
 * import { ForgotPasswordPage } from '@dream/app/auth';
 * export default function Page() {
 *   return <ForgotPasswordPage />;
 * }
 * ```
 */
export function ForgotPasswordPage({
  logo: Logo,
  footer,
  title = 'Forgot password',
  description = 'Enter your email and we\'ll send you a reset link',
}: ForgotPasswordPageProps) {
  const [submitted, setSubmitted] = useState(false);

  function handleSuccess() {
    setSubmitted(true);
  }

  return (
    <AuthLayout title={title} description={description}>
      {Logo && (
        <div className="flex justify-center mb-4">
          <Logo className="h-10" />
        </div>
      )}

      {submitted ? (
        <div className="rounded-md bg-green-50 p-4 text-sm text-green-700">
          <p className="font-medium">Check your email</p>
          <p className="mt-1">
            If an account exists with that email address, we&apos;ve sent a password reset link.
          </p>
        </div>
      ) : (
        <ForgotPasswordForm onSuccess={handleSuccess} />
      )}

      {footer ? <div className="mt-4">{footer}</div> : null}
    </AuthLayout>
  );
}
