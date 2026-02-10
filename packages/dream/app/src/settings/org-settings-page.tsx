'use client';

import React, { useState, useEffect } from 'react';

export interface OrgSettingsPageProps {
  /** API endpoint to fetch/update org settings (default: "/api/settings/organization") */
  endpoint?: string;
}

interface OrgData {
  name: string;
  slug: string;
  domain?: string;
  logoUrl?: string;
}

/**
 * Organization settings page with name, slug, and domain fields.
 *
 * Fetches current org data from the API and submits updates.
 *
 * Usage in app/settings/page.tsx:
 * ```tsx
 * import { OrgSettingsPage } from '@dream/app/settings';
 * export default function Page() {
 *   return <OrgSettingsPage />;
 * }
 * ```
 */
export function OrgSettingsPage({
  endpoint = '/api/settings/organization',
}: OrgSettingsPageProps) {
  const [data, setData] = useState<OrgData>({ name: '', slug: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    async function fetchOrg() {
      try {
        const res = await fetch(endpoint);
        if (!res.ok) throw new Error('Failed to load organization settings');
        const json = await res.json();
        setData(json.data ?? json);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load');
      } finally {
        setLoading(false);
      }
    }
    fetchOrg();
  }, [endpoint]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(false);

    try {
      const res = await fetch(endpoint, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.error?.message ?? 'Failed to save');
      }
      setSuccess(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save');
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <h1 className="text-2xl font-semibold text-slate-900">Organization Settings</h1>
        <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <div className="animate-pulse space-y-4">
            <div className="h-4 w-1/4 rounded bg-slate-200" />
            <div className="h-10 rounded bg-slate-200" />
            <div className="h-4 w-1/4 rounded bg-slate-200" />
            <div className="h-10 rounded bg-slate-200" />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold text-slate-900">Organization Settings</h1>

      <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <form onSubmit={handleSubmit} className="space-y-6">
          {error && (
            <div className="rounded-md bg-destructive/15 p-3 text-sm text-destructive">
              {error}
            </div>
          )}
          {success && (
            <div className="rounded-md bg-green-50 p-3 text-sm text-green-700">
              Settings saved successfully.
            </div>
          )}

          <div className="space-y-2">
            <label htmlFor="org-name" className="text-sm font-medium text-slate-700">
              Organization Name
            </label>
            <input
              id="org-name"
              type="text"
              value={data.name}
              onChange={(e) => setData((d) => ({ ...d, name: e.target.value }))}
              className="h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm placeholder:text-slate-400 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
              required
            />
          </div>

          <div className="space-y-2">
            <label htmlFor="org-slug" className="text-sm font-medium text-slate-700">
              Organization Slug
            </label>
            <input
              id="org-slug"
              type="text"
              value={data.slug}
              onChange={(e) =>
                setData((d) => ({
                  ...d,
                  slug: e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, ''),
                }))
              }
              className="h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm placeholder:text-slate-400 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
              required
            />
            <p className="text-xs text-slate-500">
              Used in URLs: {data.slug || 'your-org'}.example.com
            </p>
          </div>

          <div className="space-y-2">
            <label htmlFor="org-domain" className="text-sm font-medium text-slate-700">
              Custom Domain (optional)
            </label>
            <input
              id="org-domain"
              type="text"
              value={data.domain ?? ''}
              onChange={(e) => setData((d) => ({ ...d, domain: e.target.value || undefined }))}
              placeholder="app.yourcompany.com"
              className="h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm placeholder:text-slate-400 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
            />
          </div>

          <div className="flex justify-end">
            <button
              type="submit"
              disabled={saving}
              className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-primary/90 disabled:opacity-50"
            >
              {saving ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
