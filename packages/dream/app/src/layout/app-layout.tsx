'use client';

import React, { useState } from 'react';
import { Sidebar } from './sidebar';
import { Header } from './header';
import type { AppLayoutProps } from './types';

/**
 * Full application shell with collapsible sidebar and header.
 *
 * Matches Dream Payroll's exact visual structure:
 * - Sidebar: 64px collapsed / 256px expanded
 * - Header: 64px fixed height
 * - Background: slate-50
 *
 * Usage:
 * ```tsx
 * import { AppLayout } from '@dream/app/layout';
 * import { appConfig } from './config';
 *
 * export default function Layout({ children }) {
 *   return <AppLayout config={appConfig}>{children}</AppLayout>;
 * }
 * ```
 */
export function AppLayout({ children, config }: AppLayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(false);

  return (
    <div className="flex h-screen bg-slate-50">
      <Sidebar
        config={config}
        collapsed={collapsed}
        onCollapse={setCollapsed}
        mobileOpen={sidebarOpen}
        onMobileClose={() => setSidebarOpen(false)}
      />

      <div className="flex flex-1 flex-col overflow-hidden">
        <Header
          config={config}
          onMobileMenuOpen={() => setSidebarOpen(true)}
        />

        <main className="flex-1 overflow-auto p-6">{children}</main>
      </div>
    </div>
  );
}
