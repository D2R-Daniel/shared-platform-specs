'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { Search, ChevronLeft, ChevronRight, Filter } from 'lucide-react';

interface AuditRow {
  id: string;
  actorEmail: string;
  action: string;
  resourceType: string;
  resourceId: string;
  ipAddress: string;
  timestamp: string;
}

export interface AuditLogPageProps {
  /** API endpoint for audit log queries (default: "/api/audit") */
  endpoint?: string;
  /** Page size for pagination (default: 25) */
  pageSize?: number;
}

/**
 * Audit log page with a searchable, paginated table.
 *
 * Displays timestamp, actor, action, resource, and IP columns.
 * Supports filtering by action and actor.
 *
 * Usage in app/settings/audit/page.tsx:
 * ```tsx
 * import { AuditLogPage } from '@dream/app/audit';
 * export default function Page() {
 *   return <AuditLogPage />;
 * }
 * ```
 */
export function AuditLogPage({
  endpoint = '/api/audit',
  pageSize = 25,
}: AuditLogPageProps) {
  const [events, setEvents] = useState<AuditRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [searchQuery, setSearchQuery] = useState('');
  const [actionFilter, setActionFilter] = useState('');

  const fetchEvents = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams({
        page: String(page),
        pageSize: String(pageSize),
      });
      if (actionFilter) params.set('action', actionFilter);

      const res = await fetch(`${endpoint}?${params.toString()}`);
      if (!res.ok) throw new Error('Failed to load audit log');
      const json = await res.json();
      setEvents(json.data ?? json);
      if (json.meta?.totalPages) setTotalPages(json.meta.totalPages);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load');
    } finally {
      setLoading(false);
    }
  }, [endpoint, page, pageSize, actionFilter]);

  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  const filteredEvents = searchQuery
    ? events.filter(
        (e) =>
          e.actorEmail.toLowerCase().includes(searchQuery.toLowerCase()) ||
          e.action.toLowerCase().includes(searchQuery.toLowerCase()) ||
          e.resourceType.toLowerCase().includes(searchQuery.toLowerCase()),
      )
    : events;

  function formatTimestamp(ts: string): string {
    const date = new Date(ts);
    return date.toLocaleString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  }

  function formatAction(action: string): string {
    return action.replace(/\./g, ' > ').replace(/_/g, ' ');
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold text-slate-900">Audit Log</h1>

      {/* Filters */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search by actor, action, or resource..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="h-10 w-full rounded-md border border-slate-200 bg-white pl-10 pr-4 text-sm placeholder:text-slate-400 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
          />
        </div>
        <div className="relative">
          <Filter className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <select
            value={actionFilter}
            onChange={(e) => {
              setActionFilter(e.target.value);
              setPage(1);
            }}
            className="h-10 rounded-md border border-slate-200 bg-white pl-10 pr-8 text-sm focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
          >
            <option value="">All actions</option>
            <option value="auth.login">Login</option>
            <option value="auth.logout">Logout</option>
            <option value="user.created">User Created</option>
            <option value="user.updated">User Updated</option>
            <option value="role.assigned">Role Assigned</option>
            <option value="invitation.created">Invitation Created</option>
          </select>
        </div>
      </div>

      {error && (
        <div className="rounded-md bg-destructive/15 p-3 text-sm text-destructive">
          {error}
        </div>
      )}

      {/* Table */}
      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50">
                <th className="whitespace-nowrap px-4 py-3 text-left font-medium text-slate-600">
                  Timestamp
                </th>
                <th className="whitespace-nowrap px-4 py-3 text-left font-medium text-slate-600">
                  Actor
                </th>
                <th className="whitespace-nowrap px-4 py-3 text-left font-medium text-slate-600">
                  Action
                </th>
                <th className="whitespace-nowrap px-4 py-3 text-left font-medium text-slate-600">
                  Resource
                </th>
                <th className="whitespace-nowrap px-4 py-3 text-left font-medium text-slate-600">
                  IP Address
                </th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                Array.from({ length: pageSize > 10 ? 10 : pageSize }).map((_, i) => (
                  <tr key={i} className="border-b border-slate-100">
                    <td className="px-4 py-3"><div className="h-4 w-36 animate-pulse rounded bg-slate-200" /></td>
                    <td className="px-4 py-3"><div className="h-4 w-40 animate-pulse rounded bg-slate-200" /></td>
                    <td className="px-4 py-3"><div className="h-4 w-28 animate-pulse rounded bg-slate-200" /></td>
                    <td className="px-4 py-3"><div className="h-4 w-24 animate-pulse rounded bg-slate-200" /></td>
                    <td className="px-4 py-3"><div className="h-4 w-28 animate-pulse rounded bg-slate-200" /></td>
                  </tr>
                ))
              ) : filteredEvents.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-slate-500">
                    No audit events found.
                  </td>
                </tr>
              ) : (
                filteredEvents.map((event) => (
                  <tr key={event.id} className="border-b border-slate-100 hover:bg-slate-50">
                    <td className="whitespace-nowrap px-4 py-3 text-slate-500">
                      {formatTimestamp(event.timestamp)}
                    </td>
                    <td className="px-4 py-3 text-slate-900">{event.actorEmail}</td>
                    <td className="px-4 py-3">
                      <span className="inline-flex rounded-md bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-700">
                        {formatAction(event.action)}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-slate-600">
                      <span className="font-medium">{event.resourceType}</span>
                      <span className="ml-1 text-slate-400">#{event.resourceId.slice(0, 8)}</span>
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 font-mono text-xs text-slate-500">
                      {event.ipAddress}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between">
          <p className="text-sm text-slate-500">
            Page {page} of {totalPages}
          </p>
          <div className="flex gap-2">
            <button
              onClick={() => setPage((p) => Math.max(1, p - 1))}
              disabled={page <= 1}
              className="inline-flex items-center gap-1 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm text-slate-600 transition-colors hover:bg-slate-50 disabled:opacity-50"
            >
              <ChevronLeft className="h-4 w-4" />
              Previous
            </button>
            <button
              onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
              disabled={page >= totalPages}
              className="inline-flex items-center gap-1 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm text-slate-600 transition-colors hover:bg-slate-50 disabled:opacity-50"
            >
              Next
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
