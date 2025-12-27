/**
 * Express.js Integration Example
 *
 * This example shows how to integrate the shared-platform-sdk
 * into an Express.js backend application.
 */

import express, { Request, Response, NextFunction } from 'express';
import { AuthClient, UserClient, NotificationClient, UserContext } from '@platform/shared-sdk';

// Extend Express Request type to include user context
declare global {
  namespace Express {
    interface Request {
      userContext?: UserContext;
    }
  }
}

// Initialize SDK clients
const authClient = new AuthClient({
  issuerUrl: process.env.AUTH_ISSUER_URL || 'https://auth.example.com',
  clientId: process.env.AUTH_CLIENT_ID,
});

const userClient = new UserClient({
  baseUrl: process.env.API_BASE_URL || 'https://api.example.com',
});

const notificationClient = new NotificationClient({
  baseUrl: process.env.API_BASE_URL || 'https://api.example.com',
});

// Authentication middleware
export function authMiddleware(req: Request, res: Response, next: NextFunction) {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Missing or invalid authorization header' });
  }

  const token = authHeader.substring(7);

  try {
    const userContext = authClient.getUserContext(token);
    req.userContext = userContext;

    // Set token on other clients for this request
    userClient.setAccessToken(token);
    notificationClient.setAccessToken(token);

    next();
  } catch (error) {
    return res.status(401).json({ error: 'Invalid or expired token' });
  }
}

// Permission middleware factory
export function requirePermission(permission: string) {
  return (req: Request, res: Response, next: NextFunction) => {
    if (!req.userContext) {
      return res.status(401).json({ error: 'Not authenticated' });
    }

    if (!req.userContext.hasPermission(permission)) {
      return res.status(403).json({ error: 'Insufficient permissions' });
    }

    next();
  };
}

// Role middleware factory
export function requireRole(role: string) {
  return (req: Request, res: Response, next: NextFunction) => {
    if (!req.userContext) {
      return res.status(401).json({ error: 'Not authenticated' });
    }

    if (!req.userContext.hasRole(role)) {
      return res.status(403).json({ error: 'Insufficient role' });
    }

    next();
  };
}

// Admin-only middleware
export function requireAdmin(req: Request, res: Response, next: NextFunction) {
  if (!req.userContext) {
    return res.status(401).json({ error: 'Not authenticated' });
  }

  if (!req.userContext.isAdmin()) {
    return res.status(403).json({ error: 'Admin access required' });
  }

  next();
}

// Example Express app
const app = express();
app.use(express.json());

// Public route - no auth required
app.get('/health', (req, res) => {
  res.json({ status: 'ok' });
});

// Protected routes - require authentication
app.get('/api/me', authMiddleware, async (req, res) => {
  try {
    const profile = await userClient.getMyProfile();
    res.json(profile);
  } catch (error) {
    res.status(500).json({ error: 'Failed to get profile' });
  }
});

// Permission-based route
app.get('/api/users', authMiddleware, requirePermission('users:read'), async (req, res) => {
  try {
    const { page = 1, pageSize = 20, search } = req.query;
    const result = await userClient.list({
      page: Number(page),
      pageSize: Number(pageSize),
      search: search as string,
    });
    res.json(result);
  } catch (error) {
    res.status(500).json({ error: 'Failed to list users' });
  }
});

// Admin-only route
app.post('/api/users', authMiddleware, requireAdmin, async (req, res) => {
  try {
    const user = await userClient.create(req.body);
    res.status(201).json(user);
  } catch (error) {
    res.status(500).json({ error: 'Failed to create user' });
  }
});

// Notifications route
app.get('/api/notifications', authMiddleware, async (req, res) => {
  try {
    const result = await notificationClient.list({ status: 'unread' });
    res.json(result);
  } catch (error) {
    res.status(500).json({ error: 'Failed to get notifications' });
  }
});

// Mark notification as read
app.patch('/api/notifications/:id/read', authMiddleware, async (req, res) => {
  try {
    const notification = await notificationClient.markAsRead(req.params.id);
    res.json(notification);
  } catch (error) {
    res.status(500).json({ error: 'Failed to mark notification as read' });
  }
});

// Start server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});

export default app;
