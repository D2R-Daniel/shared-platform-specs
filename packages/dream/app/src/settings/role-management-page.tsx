'use client';

import React, { useState, useEffect } from 'react';
import { Shield, Plus, ChevronDown, ChevronUp } from 'lucide-react';

interface RoleRow {
  id: string;
  name: string;
  slug: string;
  description?: string;
  hierarchyLevel: number;
  isBuiltIn: boolean;
  permissions: string[];
}

export interface RoleManagementPageProps {
  /** API endpoint for role CRUD (default: "/api/roles") */
  endpoint?: string;
}

/**
 * Role management page showing a card list of roles with their permissions.
 *
 * Fetches roles from the API and displays them with expand/collapse
 * permission details.
 *
 * Usage in app/settings/roles/page.tsx:
 * ```tsx
 * import { RoleManagementPage } from '@dream/app/settings';
 * export default function Page() {
 *   return <RoleManagementPage />;
 * }
 * ```
 */
export function RoleManagementPage({
  endpoint = '/api/roles',
}: RoleManagementPageProps) {
  const [roles, setRoles] = useState<RoleRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedRole, setExpandedRole] = useState<string | null>(null);

  useEffect(() => {
    async function fetchRoles() {
      try {
        const res = await fetch(endpoint);
        if (!res.ok) throw new Error('Failed to load roles');
        const json = await res.json();
        setRoles(json.data ?? json);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load');
      } finally {
        setLoading(false);
      }
    }
    fetchRoles();
  }, [endpoint]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-slate-900">Roles & Permissions</h1>
        <button className="inline-flex items-center gap-2 rounded-md bg-primary px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-primary/90">
          <Plus className="h-4 w-4" />
          Create Role
        </button>
      </div>

      {error && (
        <div className="rounded-md bg-destructive/15 p-3 text-sm text-destructive">
          {error}
        </div>
      )}

      {loading ? (
        <div className="space-y-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
              <div className="animate-pulse space-y-3">
                <div className="h-5 w-1/4 rounded bg-slate-200" />
                <div className="h-4 w-1/2 rounded bg-slate-200" />
              </div>
            </div>
          ))}
        </div>
      ) : roles.length === 0 ? (
        <div className="rounded-lg border border-slate-200 bg-white p-8 text-center shadow-sm">
          <Shield className="mx-auto h-12 w-12 text-slate-300" />
          <p className="mt-4 text-sm text-slate-500">No roles configured yet.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {roles
            .sort((a, b) => a.hierarchyLevel - b.hierarchyLevel)
            .map((role) => {
              const isExpanded = expandedRole === role.id;
              return (
                <div
                  key={role.id}
                  className="rounded-lg border border-slate-200 bg-white shadow-sm"
                >
                  <button
                    onClick={() => setExpandedRole(isExpanded ? null : role.id)}
                    className="flex w-full items-center justify-between p-6 text-left"
                  >
                    <div className="flex items-center gap-3">
                      <Shield className="h-5 w-5 text-primary" />
                      <div>
                        <h3 className="font-medium text-slate-900">
                          {role.name}
                          {role.isBuiltIn && (
                            <span className="ml-2 inline-flex rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-500">
                              Built-in
                            </span>
                          )}
                        </h3>
                        {role.description && (
                          <p className="mt-0.5 text-sm text-slate-500">{role.description}</p>
                        )}
                      </div>
                    </div>
                    <div className="flex items-center gap-4">
                      <span className="text-sm text-slate-500">
                        {role.permissions.length} permission{role.permissions.length !== 1 ? 's' : ''}
                      </span>
                      {isExpanded ? (
                        <ChevronUp className="h-4 w-4 text-slate-400" />
                      ) : (
                        <ChevronDown className="h-4 w-4 text-slate-400" />
                      )}
                    </div>
                  </button>

                  {isExpanded && (
                    <div className="border-t border-slate-200 px-6 py-4">
                      <h4 className="mb-3 text-xs font-semibold uppercase text-slate-500">
                        Permissions
                      </h4>
                      {role.permissions.length === 0 ? (
                        <p className="text-sm text-slate-500">No permissions assigned.</p>
                      ) : (
                        <div className="flex flex-wrap gap-2">
                          {role.permissions.map((perm) => (
                            <span
                              key={perm}
                              className="inline-flex rounded-md bg-slate-100 px-2.5 py-1 text-xs font-mono text-slate-700"
                            >
                              {perm}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
        </div>
      )}
    </div>
  );
}
