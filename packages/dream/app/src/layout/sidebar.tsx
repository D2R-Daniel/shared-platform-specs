'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { ChevronLeft, ChevronRight, X } from 'lucide-react';
import type { SidebarProps } from './types';
import type { NavItem } from '../config';

function isNavActive(item: NavItem, pathname: string): boolean {
  if (item.href === '/') return pathname === '/';
  return pathname === item.href || pathname.startsWith(item.href + '/');
}

export function Sidebar({
  config,
  collapsed,
  onCollapse,
  mobileOpen,
  onMobileClose,
}: SidebarProps) {
  const [expandedItem, setExpandedItem] = useState<string | null>(null);
  const pathname = usePathname();

  const Icon = config.icon;

  return (
    <>
      {/* Mobile overlay */}
      {mobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 lg:hidden"
          onClick={onMobileClose}
        />
      )}

      <aside
        className={[
          'fixed inset-y-0 left-0 z-50 flex flex-col border-r border-slate-200 bg-white transition-all duration-200 lg:relative',
          collapsed ? 'w-16' : 'w-64',
          mobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0',
        ].join(' ')}
      >
        {/* Logo / App Name */}
        <div className="flex h-16 items-center justify-between border-b border-slate-200 px-4">
          {!collapsed && (
            <Link href="/" className="flex items-center gap-2">
              <Icon className="h-8 w-8 text-primary" />
              <span className="text-lg font-semibold text-primary">
                {config.name}
              </span>
            </Link>
          )}
          {collapsed && (
            <Icon className="mx-auto h-8 w-8 text-primary" />
          )}
          <button
            className="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600 lg:hidden"
            onClick={onMobileClose}
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto p-3">
          <ul className="space-y-1">
            {config.navigation.map((item) => {
              const isActive = isNavActive(item, pathname);
              const hasChildren = item.children && item.children.length > 0;
              const isExpanded = expandedItem === item.name;
              const ItemIcon = item.icon;

              return (
                <li key={item.name}>
                  <Link
                    href={item.href}
                    onClick={(e) => {
                      if (hasChildren) {
                        e.preventDefault();
                        setExpandedItem(isExpanded ? null : item.name);
                      }
                    }}
                    className={[
                      'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                      isActive
                        ? 'bg-primary text-white'
                        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
                    ].join(' ')}
                  >
                    <ItemIcon className="h-5 w-5 flex-shrink-0" />
                    {!collapsed && (
                      <>
                        <span className="flex-1">{item.name}</span>
                        {hasChildren && (
                          <ChevronRight
                            className={[
                              'h-4 w-4 transition-transform',
                              isExpanded ? 'rotate-90' : '',
                            ].join(' ')}
                          />
                        )}
                      </>
                    )}
                  </Link>

                  {/* Child links */}
                  {hasChildren && isExpanded && !collapsed && item.children && (
                    <ul className="ml-8 mt-1 space-y-1">
                      {item.children.map((child) => {
                        const childActive = pathname === child.href;
                        return (
                          <li key={child.name}>
                            <Link
                              href={child.href}
                              className={[
                                'block rounded-md px-3 py-2 text-sm transition-colors',
                                childActive
                                  ? 'bg-slate-100 font-medium text-primary'
                                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900',
                              ].join(' ')}
                            >
                              {child.name}
                            </Link>
                          </li>
                        );
                      })}
                    </ul>
                  )}
                </li>
              );
            })}
          </ul>
        </nav>

        {/* Collapse toggle (desktop only) */}
        <div className="hidden border-t border-slate-200 p-3 lg:block">
          <button
            onClick={() => onCollapse(!collapsed)}
            className="flex w-full items-center justify-center rounded-md p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
          >
            {collapsed ? (
              <ChevronRight className="h-5 w-5" />
            ) : (
              <ChevronLeft className="h-5 w-5" />
            )}
          </button>
        </div>
      </aside>
    </>
  );
}
