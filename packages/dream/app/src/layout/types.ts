import type { ReactNode, ComponentType } from 'react';
import type { AppConfig } from '../config';

/** Props for the main AppLayout wrapper */
export interface AppLayoutProps {
  children: ReactNode;
  config: AppConfig;
}

/** Props for the Sidebar component */
export interface SidebarProps {
  config: AppConfig;
  collapsed: boolean;
  onCollapse: (collapsed: boolean) => void;
  mobileOpen: boolean;
  onMobileClose: () => void;
}

/** Props for the Header component */
export interface HeaderProps {
  config: AppConfig;
  onMobileMenuOpen: () => void;
}

/** Notification item displayed in the header dropdown */
export interface NotificationItem {
  id: string;
  title: string;
  message: string;
  time: string;
  type: 'info' | 'warning' | 'success' | 'error';
  read?: boolean;
}
