'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { useSession, signOut } from 'next-auth/react';
import {
  Bell,
  ChevronDown,
  HelpCircle,
  LogOut,
  Menu,
  Search,
  Settings,
  User,
} from 'lucide-react';
import type { HeaderProps } from './types';

export function Header({ config, onMobileMenuOpen }: HeaderProps) {
  const [showNotifications, setShowNotifications] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const { data: session } = useSession();

  const userInitials =
    session?.user?.name
      ?.split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase() || 'U';

  const userName = session?.user?.name || 'User';
  const userEmail = session?.user?.email || '';

  return (
    <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-6">
      {/* Mobile menu button */}
      <button
        className="rounded-md p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600 lg:hidden"
        onClick={onMobileMenuOpen}
      >
        <Menu className="h-5 w-5" />
      </button>

      {/* Search */}
      <div className="hidden items-center gap-4 lg:flex">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder={`Search ${config.name}...`}
            className="h-10 w-80 rounded-md border border-slate-200 bg-slate-50 pl-10 pr-4 text-sm placeholder:text-slate-400 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/20"
          />
        </div>
      </div>

      {/* Right actions */}
      <div className="flex items-center gap-4">
        {/* Help */}
        <button className="rounded-md p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600">
          <HelpCircle className="h-5 w-5" />
        </button>

        {/* Notifications */}
        <div className="relative">
          <button
            onClick={() => {
              setShowNotifications(!showNotifications);
              setShowUserMenu(false);
            }}
            className="relative rounded-md p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
          >
            <Bell className="h-5 w-5" />
            <span className="absolute right-1 top-1 h-2 w-2 rounded-full bg-destructive" />
          </button>

          {showNotifications && (
            <div className="absolute right-0 top-full z-50 mt-2 w-80 rounded-lg border border-slate-200 bg-white shadow-lg">
              <div className="border-b border-slate-200 p-4">
                <h3 className="font-semibold text-slate-900">Notifications</h3>
              </div>
              <div className="p-6 text-center text-sm text-slate-500">
                No new notifications
              </div>
            </div>
          )}
        </div>

        {/* User menu */}
        <div className="relative">
          <button
            onClick={() => {
              setShowUserMenu(!showUserMenu);
              setShowNotifications(false);
            }}
            className="flex items-center gap-3 rounded-md p-2 hover:bg-slate-100"
          >
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-sm font-medium text-white">
              {userInitials}
            </div>
            <div className="hidden text-left md:block">
              <p className="text-sm font-medium text-slate-900">{userName}</p>
              <p className="text-xs text-slate-500">{userEmail}</p>
            </div>
            <ChevronDown className="h-4 w-4 text-slate-400" />
          </button>

          {showUserMenu && (
            <div className="absolute right-0 top-full z-50 mt-2 w-56 rounded-lg border border-slate-200 bg-white shadow-lg">
              <div className="border-b border-slate-200 p-4">
                <p className="font-medium text-slate-900">{userName}</p>
                <p className="text-sm text-slate-500">{userEmail}</p>
              </div>
              <div className="p-2">
                <Link
                  href="/settings/profile"
                  className="flex w-full items-center gap-3 rounded-md px-3 py-2 text-sm text-slate-600 hover:bg-slate-100"
                >
                  <User className="h-4 w-4" />
                  Profile Settings
                </Link>
                <Link
                  href="/settings"
                  className="flex w-full items-center gap-3 rounded-md px-3 py-2 text-sm text-slate-600 hover:bg-slate-100"
                >
                  <Settings className="h-4 w-4" />
                  Settings
                </Link>
              </div>
              <div className="border-t border-slate-200 p-2">
                <button
                  onClick={() => signOut({ callbackUrl: '/auth/login' })}
                  className="flex w-full items-center gap-3 rounded-md px-3 py-2 text-sm text-red-500 hover:bg-red-50"
                >
                  <LogOut className="h-4 w-4" />
                  Sign Out
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
