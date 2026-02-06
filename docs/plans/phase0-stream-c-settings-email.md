# Phase 0 Stream C: Settings & Email Enhancements

**Feature**: Settings versioning/history/environments and Email template versioning/i18n/providers
**Goal**: Enhance Settings with change history, environment overrides, locking, and import/export. Enhance Email with template versioning, i18n, provider abstraction, batch sending, and delivery tracking.
**Architecture**: Enhance existing clients in `src/settings/` and `src/email/` with new methods and types, following existing fetch-based patterns with `AbortController` timeouts, URL query-param serialization, and error mapping from HTTP status codes.
**Tech Stack**: TypeScript (strict mode, ES2022 target), Node.js >= 18, Vitest, native `fetch`

---

## Existing Code Patterns

Before implementing, note the following patterns observed in the current codebase:

- **HTTP Client**: Both `SettingsClient` and `EmailClient` use a private `request<T>(method, path, body?, params?)` method wrapping native `fetch` with `AbortController` timeout, `Bearer` auth, JSON serialization, and status-code-based error mapping.
- **Error Hierarchy**: Each module defines a base error class (`SettingsError`, `EmailError`) extending `Error`, with specific subclasses that carry contextual `readonly` properties (e.g., `key`, `identifier`, `slug`).
- **Types**: Interfaces use `snake_case` property names matching the API. Union types are used for enums (e.g., `SettingCategory`, `TemplateCategory`). Pagination follows `{ data: T[], total, page, page_size }`.
- **Index Exports**: Each module's `index.ts` re-exports `* from './types'`, `* from './errors'`, and named exports from `'./client'`.
- **Tests**: Tests use Vitest (`describe`, `it`, `expect`, `vi`, `beforeEach`). Existing tests for fetch-based clients would mock `global.fetch` via `vi.fn()`. Tests for axios-based clients mock `axios.create`.
- **File Location**: Test files live in `src/__tests__/{module}.test.ts`.

---

## Tasks

### Task 1: Settings History & Environment Types

**Files**:
- Modify: `packages/node/src/settings/types.ts`

**Steps**:

1. Add the `ChangeSource` union type after the existing `SettingType`:

```typescript
export type ChangeSource = 'api' | 'dashboard' | 'import' | 'system';
```

2. Add the `Environment` union type:

```typescript
export type Environment = 'development' | 'staging' | 'production';
```

3. Add the `SettingSource` union type:

```typescript
export type SettingSource = 'platform' | 'tenant' | 'user' | 'environment_override';
```

4. Add the `ImportStrategy` union type:

```typescript
export type ImportStrategy = 'overwrite' | 'skip_existing' | 'merge';
```

5. Add the `ExportFormat` union type:

```typescript
export type ExportFormat = 'json' | 'yaml';
```

6. Add the `SettingChange` interface:

```typescript
export interface SettingChange {
  id: string;
  tenant_id: string;
  key: string;
  old_value: unknown;
  new_value: unknown;
  changed_by: string;
  changed_at: string;
  change_source: ChangeSource;
}
```

7. Add the `SettingChangeListResponse` interface:

```typescript
export interface SettingChangeListResponse {
  data: SettingChange[];
  total: number;
  page: number;
  page_size: number;
}
```

8. Add the `EnvironmentOverride` interface:

```typescript
export interface EnvironmentOverride {
  key: string;
  environment: Environment;
  value: unknown;
  overridden_at: string;
  overridden_by: string;
}
```

9. Add the `EnvironmentOverrideListResponse` interface:

```typescript
export interface EnvironmentOverrideListResponse {
  data: EnvironmentOverride[];
  total: number;
  page: number;
  page_size: number;
}
```

10. Add the `ExportResult` interface:

```typescript
export interface ExportResult {
  format: ExportFormat;
  data: string;
  exported_at: string;
  category_count: number;
  setting_count: number;
  tenant_id: string;
}
```

11. Add the `ImportError` interface:

```typescript
export interface ImportError {
  key: string;
  reason: string;
  value?: unknown;
}
```

12. Add the `ImportResult` interface:

```typescript
export interface ImportResult {
  imported_count: number;
  skipped_count: number;
  error_count: number;
  errors: ImportError[];
}
```

13. Add the `ImportSettingsRequest` interface:

```typescript
export interface ImportSettingsRequest {
  data: string;
  format?: ExportFormat;
  strategy?: ImportStrategy;
}
```

14. Add the `LockedSetting` interface:

```typescript
export interface LockedSetting {
  key: string;
  locked_by: string;
  locked_at: string;
  reason?: string;
  locked_value: unknown;
}
```

15. Add the `LockedSettingListResponse` interface:

```typescript
export interface LockedSettingListResponse {
  data: LockedSetting[];
  total: number;
  page: number;
  page_size: number;
}
```

16. Add the `EffectiveSetting` interface:

```typescript
export interface EffectiveSetting {
  key: string;
  value: unknown;
  source: SettingSource;
  inherited: boolean;
  definition?: SettingDefinition;
}
```

17. Add the `BulkUpdateItem` interface:

```typescript
export interface BulkUpdateItem {
  key: string;
  value: unknown;
}
```

18. Add the `BulkUpdateError` interface:

```typescript
export interface BulkUpdateError {
  key: string;
  reason: string;
}
```

19. Add the `BulkUpdateResult` interface:

```typescript
export interface BulkUpdateResult {
  updated_count: number;
  skipped_count: number;
  errors: BulkUpdateError[];
}
```

**Tests**: No tests needed for pure type definitions (TypeScript compiler validates).
**Commit**: `feat(settings): add types for history, environments, locking, import/export, and bulk update`

---

### Task 2: Settings Error Classes

**Files**:
- Modify: `packages/node/src/settings/errors.ts`

**Steps**:

1. Add `SettingLockedError` after the existing `InvalidCategoryError`:

```typescript
export class SettingLockedError extends SettingsError {
  public readonly key: string;
  public readonly lockedBy?: string;

  constructor(key: string, lockedBy?: string) {
    const msg = lockedBy
      ? `Setting '${key}' is locked by ${lockedBy}`
      : `Setting '${key}' is locked`;
    super(msg);
    this.name = 'SettingLockedError';
    this.key = key;
    this.lockedBy = lockedBy;
  }
}
```

2. Add `ReadonlySettingError`:

```typescript
export class ReadonlySettingError extends SettingsError {
  public readonly key: string;

  constructor(key: string) {
    super(`Setting '${key}' is readonly and cannot be modified`);
    this.name = 'ReadonlySettingError';
    this.key = key;
  }
}
```

3. Add `ImportValidationError`:

```typescript
export class ImportValidationError extends SettingsError {
  public readonly errors: Array<{ key: string; reason: string }>;

  constructor(errors: Array<{ key: string; reason: string }>) {
    super(`Import validation failed with ${errors.length} error(s)`);
    this.name = 'ImportValidationError';
    this.errors = errors;
  }
}
```

4. Add `InvalidEnvironmentError`:

```typescript
export class InvalidEnvironmentError extends SettingsError {
  public readonly environment: string;

  constructor(environment: string) {
    super(`Invalid environment: ${environment}`);
    this.name = 'InvalidEnvironmentError';
    this.environment = environment;
  }
}
```

**Tests**: No tests needed for pure error class definitions.
**Commit**: `feat(settings): add error classes for locking, readonly, import, and environment`

---

### Task 3: Settings History Methods

**Files**:
- Modify: `packages/node/src/settings/client.ts`
- Create: `packages/node/src/__tests__/settings.test.ts`

**Steps**:

1. Add imports to `client.ts` for the new types:

```typescript
import {
  SettingCategory,
  SettingDefinition,
  SettingValue,
  AllSettingsResponse,
  CategorySettingsResponse,
  GetDefinitionsParams,
  SettingChangeListResponse,
} from './types';
```

2. Add `getSettingHistory` method to `SettingsClient` (after the `set` method):

```typescript
  // History Operations

  async getSettingHistory(
    key: string,
    page?: number,
    pageSize?: number
  ): Promise<SettingChangeListResponse> {
    try {
      return await this.request('GET', `/settings/${key}/history`, undefined, {
        page,
        page_size: pageSize,
      });
    } catch (error) {
      if (error instanceof SettingNotFoundError) {
        throw new SettingNotFoundError(key);
      }
      throw error;
    }
  }
```

3. Add `getCategoryHistory` method:

```typescript
  async getCategoryHistory(
    category: SettingCategory,
    page?: number,
    pageSize?: number
  ): Promise<SettingChangeListResponse> {
    try {
      return await this.request('GET', `/settings/${category}/history`, undefined, {
        page,
        page_size: pageSize,
      });
    } catch (error) {
      if (error instanceof InvalidSettingValueError) {
        throw new InvalidCategoryError(category);
      }
      throw error;
    }
  }
```

4. Create the test file `src/__tests__/settings.test.ts` with a `global.fetch` mock pattern:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { SettingsClient } from '../settings';
import {
  SettingNotFoundError,
  InvalidCategoryError,
} from '../settings/errors';

// Mock global fetch
const mockFetch = vi.fn();
global.fetch = mockFetch;

function createJsonResponse(data: unknown, status = 200): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    statusText: 'OK',
    json: () => Promise.resolve(data),
    headers: new Headers(),
    redirected: false,
    type: 'basic',
    url: '',
    clone: () => ({} as Response),
    body: null,
    bodyUsed: false,
    arrayBuffer: () => Promise.resolve(new ArrayBuffer(0)),
    blob: () => Promise.resolve(new Blob()),
    formData: () => Promise.resolve(new FormData()),
    text: () => Promise.resolve(''),
  } as Response;
}

function createErrorResponse(status: number, data: unknown = {}): Response {
  return {
    ok: false,
    status,
    statusText: 'Error',
    json: () => Promise.resolve(data),
    headers: new Headers(),
    redirected: false,
    type: 'basic',
    url: '',
    clone: () => ({} as Response),
    body: null,
    bodyUsed: false,
    arrayBuffer: () => Promise.resolve(new ArrayBuffer(0)),
    blob: () => Promise.resolve(new Blob()),
    formData: () => Promise.resolve(new FormData()),
    text: () => Promise.resolve(''),
  } as Response;
}

describe('SettingsClient', () => {
  let client: SettingsClient;

  beforeEach(() => {
    vi.clearAllMocks();
    client = new SettingsClient({
      baseUrl: 'https://api.example.com',
      accessToken: 'test-token',
    });
  });

  describe('getSettingHistory', () => {
    it('should return change history for a setting key', async () => {
      const mockHistory = {
        data: [
          {
            id: 'change-1',
            tenant_id: 'tenant-1',
            key: 'general.site_name',
            old_value: 'Old Name',
            new_value: 'New Name',
            changed_by: 'user-1',
            changed_at: '2024-01-15T10:00:00Z',
            change_source: 'api',
          },
        ],
        total: 1,
        page: 1,
        page_size: 20,
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockHistory));

      const result = await client.getSettingHistory('general.site_name');

      expect(result.data).toHaveLength(1);
      expect(result.data[0].key).toBe('general.site_name');
      expect(result.data[0].change_source).toBe('api');
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/settings/general.site_name/history'),
        expect.objectContaining({ method: 'GET' })
      );
    });

    it('should pass pagination params', async () => {
      mockFetch.mockResolvedValueOnce(
        createJsonResponse({ data: [], total: 0, page: 2, page_size: 10 })
      );

      await client.getSettingHistory('general.site_name', 2, 10);

      const calledUrl = mockFetch.mock.calls[0][0] as string;
      expect(calledUrl).toContain('page=2');
      expect(calledUrl).toContain('page_size=10');
    });

    it('should throw SettingNotFoundError for 404', async () => {
      mockFetch.mockResolvedValueOnce(createErrorResponse(404));

      await expect(client.getSettingHistory('nonexistent.key')).rejects.toThrow(
        SettingNotFoundError
      );
    });
  });

  describe('getCategoryHistory', () => {
    it('should return change history for a category', async () => {
      const mockHistory = {
        data: [
          {
            id: 'change-2',
            tenant_id: 'tenant-1',
            key: 'general.timezone',
            old_value: 'UTC',
            new_value: 'US/Eastern',
            changed_by: 'user-1',
            changed_at: '2024-01-15T10:00:00Z',
            change_source: 'dashboard',
          },
        ],
        total: 1,
        page: 1,
        page_size: 20,
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockHistory));

      const result = await client.getCategoryHistory('general');

      expect(result.data).toHaveLength(1);
      expect(result.data[0].change_source).toBe('dashboard');
    });
  });
});
```

**Tests**:
- `getSettingHistory` returns paginated change list
- `getSettingHistory` passes pagination query params
- `getSettingHistory` throws `SettingNotFoundError` on 404
- `getCategoryHistory` returns paginated change list for a category

**Commit**: `feat(settings): add setting and category history methods`

---

### Task 4: Settings Environment Override Methods

**Files**:
- Modify: `packages/node/src/settings/client.ts`
- Modify: `packages/node/src/__tests__/settings.test.ts`

**Steps**:

1. Add additional type imports to `client.ts`:

```typescript
import {
  // ... existing imports ...
  Environment,
  EnvironmentOverride,
  EnvironmentOverrideListResponse,
} from './types';
import {
  // ... existing imports ...
  InvalidEnvironmentError,
} from './errors';
```

2. Add `getForEnvironment` method to `SettingsClient`:

```typescript
  // Environment Override Operations

  async getForEnvironment(key: string, environment: Environment): Promise<SettingValue> {
    try {
      return await this.request('GET', `/settings/${key}/environments/${environment}`);
    } catch (error) {
      if (error instanceof SettingNotFoundError) {
        throw new SettingNotFoundError(key);
      }
      throw error;
    }
  }
```

3. Add `setForEnvironment` method:

```typescript
  async setForEnvironment(
    key: string,
    value: unknown,
    environment: Environment
  ): Promise<EnvironmentOverride> {
    try {
      return await this.request(
        'PUT',
        `/settings/${key}/environments/${environment}`,
        { value }
      );
    } catch (error) {
      if (error instanceof SettingNotFoundError) {
        throw new SettingNotFoundError(key);
      }
      throw error;
    }
  }
```

4. Add `listEnvironmentOverrides` method:

```typescript
  async listEnvironmentOverrides(
    environment: Environment
  ): Promise<EnvironmentOverrideListResponse> {
    return this.request('GET', `/settings/environments/${environment}`);
  }
```

5. Add tests to `settings.test.ts`:

```typescript
  describe('getForEnvironment', () => {
    it('should get environment-specific setting value', async () => {
      const mockValue = {
        key: 'general.debug_mode',
        value: true,
        definition: undefined,
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockValue));

      const result = await client.getForEnvironment('general.debug_mode', 'staging');

      expect(result.key).toBe('general.debug_mode');
      expect(result.value).toBe(true);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/settings/general.debug_mode/environments/staging'),
        expect.objectContaining({ method: 'GET' })
      );
    });

    it('should throw SettingNotFoundError on 404', async () => {
      mockFetch.mockResolvedValueOnce(createErrorResponse(404));

      await expect(
        client.getForEnvironment('nonexistent.key', 'production')
      ).rejects.toThrow(SettingNotFoundError);
    });
  });

  describe('setForEnvironment', () => {
    it('should set environment-specific value', async () => {
      const mockOverride = {
        key: 'general.debug_mode',
        environment: 'staging',
        value: true,
        overridden_at: '2024-01-15T10:00:00Z',
        overridden_by: 'user-1',
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockOverride));

      const result = await client.setForEnvironment('general.debug_mode', true, 'staging');

      expect(result.environment).toBe('staging');
      expect(result.value).toBe(true);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/settings/general.debug_mode/environments/staging'),
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({ value: true }),
        })
      );
    });
  });

  describe('listEnvironmentOverrides', () => {
    it('should list all overrides for an environment', async () => {
      const mockResponse = {
        data: [
          {
            key: 'general.debug_mode',
            environment: 'staging',
            value: true,
            overridden_at: '2024-01-15T10:00:00Z',
            overridden_by: 'user-1',
          },
        ],
        total: 1,
        page: 1,
        page_size: 20,
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockResponse));

      const result = await client.listEnvironmentOverrides('staging');

      expect(result.data).toHaveLength(1);
      expect(result.data[0].environment).toBe('staging');
    });
  });
```

**Tests**:
- `getForEnvironment` retrieves environment-specific value
- `getForEnvironment` throws `SettingNotFoundError` on 404
- `setForEnvironment` sends PUT with value body
- `listEnvironmentOverrides` returns paginated list

**Commit**: `feat(settings): add environment override methods`

---

### Task 5: Settings Import/Export Methods

**Files**:
- Modify: `packages/node/src/settings/client.ts`
- Modify: `packages/node/src/__tests__/settings.test.ts`

**Steps**:

1. Add type imports to `client.ts`:

```typescript
import {
  // ... existing imports ...
  ExportFormat,
  ExportResult,
  ImportSettingsRequest,
  ImportResult,
} from './types';
import {
  // ... existing imports ...
  ImportValidationError,
} from './errors';
```

2. Add `exportSettings` method:

```typescript
  // Import/Export Operations

  async exportSettings(
    format?: ExportFormat,
    categories?: SettingCategory[]
  ): Promise<ExportResult> {
    return this.request('GET', '/settings/export', undefined, {
      format,
      categories: categories?.join(','),
    });
  }
```

3. Add `importSettings` method with error mapping for 422 (validation):

```typescript
  async importSettings(
    data: string,
    format?: ExportFormat,
    strategy?: ImportStrategy
  ): Promise<ImportResult> {
    try {
      return await this.request('POST', '/settings/import', {
        data,
        format: format ?? 'json',
        strategy: strategy ?? 'merge',
      });
    } catch (error) {
      if (error instanceof Error && error.message.includes('422')) {
        throw new ImportValidationError([]);
      }
      throw error;
    }
  }
```

4. Update the `request` method in `client.ts` to also handle 422 status codes by adding after the 404 block:

```typescript
        if (status === 422) {
          const errors = (data.errors as Array<{ key: string; reason: string }>) || [];
          throw new ImportValidationError(errors);
        }
```

5. Add `ImportStrategy` to the imports from `./types` in `client.ts`:

```typescript
import {
  // ... all imports ...
  ImportStrategy,
} from './types';
```

6. Add tests:

```typescript
  describe('exportSettings', () => {
    it('should export settings as JSON', async () => {
      const mockExport = {
        format: 'json',
        data: '{"general": {"site_name": "My Site"}}',
        exported_at: '2024-01-15T10:00:00Z',
        category_count: 1,
        setting_count: 1,
        tenant_id: 'tenant-1',
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockExport));

      const result = await client.exportSettings('json');

      expect(result.format).toBe('json');
      expect(result.category_count).toBe(1);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/settings/export'),
        expect.objectContaining({ method: 'GET' })
      );
    });

    it('should pass format and categories as query params', async () => {
      mockFetch.mockResolvedValueOnce(
        createJsonResponse({
          format: 'yaml',
          data: '',
          exported_at: '',
          category_count: 0,
          setting_count: 0,
          tenant_id: 'tenant-1',
        })
      );

      await client.exportSettings('yaml', ['general', 'branding']);

      const calledUrl = mockFetch.mock.calls[0][0] as string;
      expect(calledUrl).toContain('format=yaml');
      expect(calledUrl).toContain('categories=general%2Cbranding');
    });
  });

  describe('importSettings', () => {
    it('should import settings with merge strategy', async () => {
      const mockImport = {
        imported_count: 5,
        skipped_count: 1,
        error_count: 0,
        errors: [],
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockImport));

      const result = await client.importSettings(
        '{"general": {"site_name": "Imported"}}',
        'json',
        'merge'
      );

      expect(result.imported_count).toBe(5);
      expect(result.errors).toHaveLength(0);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/settings/import'),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({
            data: '{"general": {"site_name": "Imported"}}',
            format: 'json',
            strategy: 'merge',
          }),
        })
      );
    });

    it('should throw ImportValidationError on 422', async () => {
      mockFetch.mockResolvedValueOnce(
        createErrorResponse(422, {
          errors: [{ key: 'invalid.key', reason: 'Unknown setting' }],
        })
      );

      await expect(
        client.importSettings('{"bad": "data"}')
      ).rejects.toThrow('Import validation failed');
    });
  });
```

**Tests**:
- `exportSettings` returns export data with correct format
- `exportSettings` passes format and categories as query params
- `importSettings` sends POST with data/format/strategy body
- `importSettings` throws `ImportValidationError` on 422

**Commit**: `feat(settings): add import/export methods`

---

### Task 6: Settings Locking Methods

**Files**:
- Modify: `packages/node/src/settings/client.ts`
- Modify: `packages/node/src/__tests__/settings.test.ts`

**Steps**:

1. Add type imports to `client.ts`:

```typescript
import {
  // ... existing imports ...
  LockedSetting,
  LockedSettingListResponse,
} from './types';
import {
  // ... existing imports ...
  SettingLockedError,
} from './errors';
```

2. Update `request` method to handle 423 (Locked) status by adding after the 422 block:

```typescript
        if (status === 423) {
          const key = (data.key as string) || 'unknown';
          const lockedBy = (data.locked_by as string) || undefined;
          throw new SettingLockedError(key, lockedBy);
        }
```

3. Add `lockSetting` method:

```typescript
  // Locking Operations

  async lockSetting(key: string, reason?: string): Promise<LockedSetting> {
    try {
      return await this.request('POST', `/settings/${key}/lock`, {
        reason,
      });
    } catch (error) {
      if (error instanceof SettingNotFoundError) {
        throw new SettingNotFoundError(key);
      }
      throw error;
    }
  }
```

4. Add `unlockSetting` method:

```typescript
  async unlockSetting(key: string): Promise<void> {
    try {
      await this.request('POST', `/settings/${key}/unlock`);
    } catch (error) {
      if (error instanceof SettingNotFoundError) {
        throw new SettingNotFoundError(key);
      }
      throw error;
    }
  }
```

5. Add `listLockedSettings` method:

```typescript
  async listLockedSettings(): Promise<LockedSettingListResponse> {
    return this.request('GET', '/settings/locked');
  }
```

6. Add tests:

```typescript
  describe('lockSetting', () => {
    it('should lock a setting with a reason', async () => {
      const mockLocked = {
        key: 'security.mfa_required',
        locked_by: 'admin-1',
        locked_at: '2024-01-15T10:00:00Z',
        reason: 'Compliance requirement',
        locked_value: true,
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockLocked));

      const result = await client.lockSetting('security.mfa_required', 'Compliance requirement');

      expect(result.key).toBe('security.mfa_required');
      expect(result.reason).toBe('Compliance requirement');
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/settings/security.mfa_required/lock'),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ reason: 'Compliance requirement' }),
        })
      );
    });

    it('should throw SettingNotFoundError on 404', async () => {
      mockFetch.mockResolvedValueOnce(createErrorResponse(404));

      await expect(client.lockSetting('nonexistent.key')).rejects.toThrow(SettingNotFoundError);
    });
  });

  describe('unlockSetting', () => {
    it('should unlock a setting', async () => {
      mockFetch.mockResolvedValueOnce(createJsonResponse(undefined, 204));

      await client.unlockSetting('security.mfa_required');

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/settings/security.mfa_required/unlock'),
        expect.objectContaining({ method: 'POST' })
      );
    });
  });

  describe('listLockedSettings', () => {
    it('should list all locked settings', async () => {
      const mockResponse = {
        data: [
          {
            key: 'security.mfa_required',
            locked_by: 'admin-1',
            locked_at: '2024-01-15T10:00:00Z',
            reason: 'Compliance',
            locked_value: true,
          },
        ],
        total: 1,
        page: 1,
        page_size: 20,
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockResponse));

      const result = await client.listLockedSettings();

      expect(result.data).toHaveLength(1);
      expect(result.data[0].locked_by).toBe('admin-1');
    });
  });
```

**Tests**:
- `lockSetting` sends POST with reason body
- `lockSetting` throws `SettingNotFoundError` on 404
- `unlockSetting` sends POST (expects 204)
- `listLockedSettings` returns paginated list

**Commit**: `feat(settings): add setting lock/unlock methods`

---

### Task 7: Settings Effective Value, Sensitive, and Bulk Update Methods

**Files**:
- Modify: `packages/node/src/settings/client.ts`
- Modify: `packages/node/src/__tests__/settings.test.ts`

**Steps**:

1. Add type imports to `client.ts`:

```typescript
import {
  // ... existing imports ...
  EffectiveSetting,
  BulkUpdateItem,
  BulkUpdateResult,
} from './types';
import {
  // ... existing imports ...
  ReadonlySettingError,
} from './errors';
```

2. Update `request` method to handle 403 for readonly settings, adding after 400 block:

```typescript
        if (status === 403) {
          const key = (data.key as string) || 'unknown';
          if (data.code === 'readonly_setting') {
            throw new ReadonlySettingError(key);
          }
          throw new Error(`HTTP 403: Forbidden`);
        }
```

3. Add `getEffectiveSetting` method:

```typescript
  // Advanced Operations

  async getEffectiveSetting(
    key: string,
    userId?: string,
    environment?: Environment
  ): Promise<EffectiveSetting> {
    try {
      return await this.request('GET', `/settings/${key}/effective`, undefined, {
        user_id: userId,
        environment,
      });
    } catch (error) {
      if (error instanceof SettingNotFoundError) {
        throw new SettingNotFoundError(key);
      }
      throw error;
    }
  }
```

4. Add `getSensitiveValue` method:

```typescript
  async getSensitiveValue(key: string): Promise<SettingValue> {
    try {
      return await this.request('GET', `/settings/${key}/sensitive`);
    } catch (error) {
      if (error instanceof SettingNotFoundError) {
        throw new SettingNotFoundError(key);
      }
      throw error;
    }
  }
```

5. Add `bulkUpdate` method:

```typescript
  async bulkUpdate(settings: BulkUpdateItem[]): Promise<BulkUpdateResult> {
    return this.request('PUT', '/settings/bulk', { settings });
  }
```

6. Add tests:

```typescript
  describe('getEffectiveSetting', () => {
    it('should get effective setting with source info', async () => {
      const mockEffective = {
        key: 'features.dark_mode',
        value: true,
        source: 'environment_override',
        inherited: false,
        definition: {
          key: 'features.dark_mode',
          type: 'boolean',
          label: 'Dark Mode',
          category: 'features',
          is_public: true,
          is_readonly: false,
          display_order: 1,
        },
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockEffective));

      const result = await client.getEffectiveSetting(
        'features.dark_mode',
        'user-1',
        'staging'
      );

      expect(result.source).toBe('environment_override');
      expect(result.inherited).toBe(false);
      expect(result.value).toBe(true);

      const calledUrl = mockFetch.mock.calls[0][0] as string;
      expect(calledUrl).toContain('user_id=user-1');
      expect(calledUrl).toContain('environment=staging');
    });

    it('should throw SettingNotFoundError on 404', async () => {
      mockFetch.mockResolvedValueOnce(createErrorResponse(404));

      await expect(client.getEffectiveSetting('nonexistent.key')).rejects.toThrow(
        SettingNotFoundError
      );
    });
  });

  describe('getSensitiveValue', () => {
    it('should retrieve a sensitive setting value', async () => {
      const mockValue = {
        key: 'integrations.api_secret',
        value: 'decrypted-secret-value',
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockValue));

      const result = await client.getSensitiveValue('integrations.api_secret');

      expect(result.key).toBe('integrations.api_secret');
      expect(result.value).toBe('decrypted-secret-value');
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/settings/integrations.api_secret/sensitive'),
        expect.objectContaining({ method: 'GET' })
      );
    });
  });

  describe('bulkUpdate', () => {
    it('should bulk update multiple settings', async () => {
      const mockResult = {
        updated_count: 2,
        skipped_count: 0,
        errors: [],
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockResult));

      const settings = [
        { key: 'general.site_name', value: 'New Name' },
        { key: 'general.timezone', value: 'US/Pacific' },
      ];

      const result = await client.bulkUpdate(settings);

      expect(result.updated_count).toBe(2);
      expect(result.errors).toHaveLength(0);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/settings/bulk'),
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({ settings }),
        })
      );
    });

    it('should return partial results with errors', async () => {
      const mockResult = {
        updated_count: 1,
        skipped_count: 0,
        errors: [{ key: 'security.mfa_required', reason: 'Setting is locked' }],
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockResult));

      const result = await client.bulkUpdate([
        { key: 'general.site_name', value: 'New' },
        { key: 'security.mfa_required', value: false },
      ]);

      expect(result.updated_count).toBe(1);
      expect(result.errors).toHaveLength(1);
      expect(result.errors[0].key).toBe('security.mfa_required');
    });
  });
```

**Tests**:
- `getEffectiveSetting` returns effective value with source and passes userId/environment params
- `getEffectiveSetting` throws `SettingNotFoundError` on 404
- `getSensitiveValue` retrieves decrypted value
- `bulkUpdate` sends PUT with array of settings
- `bulkUpdate` returns partial results with error details

**Commit**: `feat(settings): add effective settings, sensitive values, and bulk update methods`

---

### Task 8: Settings Module Index Exports Update

**Files**:
- Modify: `packages/node/src/settings/index.ts`
- Modify: `packages/node/src/index.ts`

**Steps**:

1. The `settings/index.ts` already re-exports `* from './types'` and `* from './errors'`, so all new types and errors are automatically exported. No changes needed to `settings/index.ts`.

2. Update the settings type exports in `src/index.ts` to include the new types:

```typescript
export type {
  // Settings types
  SettingCategory,
  SettingType,
  SettingDefinition,
  TenantSettings,
  SettingValue,
  AllSettingsResponse,
  CategorySettingsResponse,
  // New types
  ChangeSource,
  Environment,
  SettingSource,
  ImportStrategy,
  ExportFormat,
  SettingChange,
  SettingChangeListResponse,
  EnvironmentOverride,
  EnvironmentOverrideListResponse,
  ExportResult,
  ImportError,
  ImportResult,
  ImportSettingsRequest,
  LockedSetting,
  LockedSettingListResponse,
  EffectiveSetting,
  BulkUpdateItem,
  BulkUpdateError,
  BulkUpdateResult,
} from './settings';
```

**Tests**: Run `npm run typecheck` to ensure all exports resolve correctly.
**Commit**: `feat(settings): update module exports for all new settings types`

---

### Task 9: Email Versioning & i18n Types

**Files**:
- Modify: `packages/node/src/email/types.ts`

**Steps**:

1. Add the `ProviderType` union type after the existing `TemplateCategory`:

```typescript
export type ProviderType = 'smtp' | 'sendgrid' | 'ses' | 'resend' | 'postmark';
```

2. Add the `EmailSendStatus` union type:

```typescript
export type EmailSendStatus = 'queued' | 'sent' | 'delivered' | 'bounced' | 'failed';
```

3. Add the `DeliveryEventType` union type:

```typescript
export type DeliveryEventType = 'delivered' | 'opened' | 'clicked' | 'bounced' | 'complained';
```

4. Add the `TemplateVersion` interface:

```typescript
export interface TemplateVersion {
  version: number;
  template_id: string;
  html_content: string;
  text_content?: string;
  subject: string;
  variables: string[];
  change_notes?: string;
  created_at: string;
  created_by: string;
}
```

5. Add the `TemplateVersionListResponse` interface:

```typescript
export interface TemplateVersionListResponse {
  data: TemplateVersion[];
  total: number;
  page: number;
  page_size: number;
}
```

6. Add the `TemplateLocale` interface:

```typescript
export interface TemplateLocale {
  template_id: string;
  locale: string;
  subject: string;
  html_content: string;
  text_content?: string;
  variables: string[];
}
```

7. Add the `TemplateLocaleListResponse` interface:

```typescript
export interface TemplateLocaleListResponse {
  data: TemplateLocale[];
  total: number;
}
```

8. Add the `TemplatePreview` interface:

```typescript
export interface TemplatePreview {
  subject: string;
  html_content: string;
  text_content?: string;
}
```

9. Add the `SetTemplateLocaleRequest` interface:

```typescript
export interface SetTemplateLocaleRequest {
  subject: string;
  html_content: string;
  text_content?: string;
  variables?: string[];
}
```

10. Add the `Attachment` interface:

```typescript
export interface Attachment {
  filename: string;
  content_type: string;
  content: string;
  size_bytes: number;
}
```

11. Add the `BatchRecipient` interface:

```typescript
export interface BatchRecipient {
  to: string;
  variables: Record<string, string>;
  locale?: string;
  metadata?: Record<string, unknown>;
}
```

12. Add the `BatchSendRequest` interface:

```typescript
export interface BatchSendRequest {
  template_slug: string;
  recipients: BatchRecipient[];
  locale?: string;
}
```

13. Add the `BatchSendFailure` interface:

```typescript
export interface BatchSendFailure {
  to: string;
  error: string;
}
```

14. Add the `BatchSendResult` interface:

```typescript
export interface BatchSendResult {
  batch_id: string;
  total: number;
  sent_count: number;
  failed_count: number;
  failures: BatchSendFailure[];
}
```

15. Add the `EmailSendRecord` interface:

```typescript
export interface EmailSendRecord {
  message_id: string;
  template_id?: string;
  template_slug?: string;
  to: string[];
  cc?: string[];
  bcc?: string[];
  subject: string;
  status: EmailSendStatus;
  provider?: string;
  sent_at?: string;
  delivered_at?: string;
  error?: string;
  metadata?: Record<string, unknown>;
}
```

16. Add the `SendHistoryListResponse` interface:

```typescript
export interface SendHistoryListResponse {
  data: EmailSendRecord[];
  total: number;
  page: number;
  page_size: number;
}
```

17. Add the `ListSendHistoryParams` interface:

```typescript
export interface ListSendHistoryParams {
  page?: number;
  page_size?: number;
  status?: EmailSendStatus;
  template_slug?: string;
  to?: string;
  from_date?: string;
  to_date?: string;
}
```

18. Add the `EmailDeliveryEvent` interface:

```typescript
export interface EmailDeliveryEvent {
  message_id: string;
  event_type: DeliveryEventType;
  timestamp: string;
  metadata?: Record<string, unknown>;
}
```

19. Add the `EmailSendDetails` interface:

```typescript
export interface EmailSendDetails {
  record: EmailSendRecord;
  events: EmailDeliveryEvent[];
}
```

20. Add the `EmailProvider` interface:

```typescript
export interface EmailProvider {
  type: ProviderType;
  config: Record<string, unknown>;
  is_active: boolean;
  verified_at?: string;
}
```

21. Add the `ConfigureProviderRequest` interface:

```typescript
export interface ConfigureProviderRequest {
  type: ProviderType;
  config: Record<string, unknown>;
  is_active?: boolean;
}
```

22. Add the `EmailProviderListResponse` interface:

```typescript
export interface EmailProviderListResponse {
  data: EmailProvider[];
  total: number;
}
```

**Tests**: No tests needed for pure type definitions.
**Commit**: `feat(email): add types for versioning, i18n, providers, batch send, and delivery tracking`

---

### Task 10: Email Error Classes

**Files**:
- Modify: `packages/node/src/email/errors.ts`

**Steps**:

1. Add `LocaleNotFoundError` after the existing `EmailConfigError`:

```typescript
export class LocaleNotFoundError extends EmailError {
  public readonly templateId: string;
  public readonly locale: string;

  constructor(templateId: string, locale: string) {
    super(`Locale '${locale}' not found for template '${templateId}'`);
    this.name = 'LocaleNotFoundError';
    this.templateId = templateId;
    this.locale = locale;
  }
}
```

2. Add `VersionNotFoundError`:

```typescript
export class VersionNotFoundError extends EmailError {
  public readonly templateId: string;
  public readonly version: number;

  constructor(templateId: string, version: number) {
    super(`Version ${version} not found for template '${templateId}'`);
    this.name = 'VersionNotFoundError';
    this.templateId = templateId;
    this.version = version;
  }
}
```

3. Add `AttachmentTooLargeError`:

```typescript
export class AttachmentTooLargeError extends EmailError {
  public readonly filename: string;
  public readonly sizeBytes: number;
  public readonly maxBytes: number;

  constructor(filename: string, sizeBytes: number, maxBytes: number) {
    super(
      `Attachment '${filename}' is ${sizeBytes} bytes, exceeding limit of ${maxBytes} bytes`
    );
    this.name = 'AttachmentTooLargeError';
    this.filename = filename;
    this.sizeBytes = sizeBytes;
    this.maxBytes = maxBytes;
  }
}
```

4. Add `BatchTooLargeError`:

```typescript
export class BatchTooLargeError extends EmailError {
  public readonly recipientCount: number;
  public readonly maxRecipients: number;

  constructor(recipientCount: number, maxRecipients: number) {
    super(
      `Batch contains ${recipientCount} recipients, exceeding limit of ${maxRecipients}`
    );
    this.name = 'BatchTooLargeError';
    this.recipientCount = recipientCount;
    this.maxRecipients = maxRecipients;
  }
}
```

5. Add `ProviderConfigError`:

```typescript
export class ProviderConfigError extends EmailError {
  public readonly providerType: string;

  constructor(providerType: string, message?: string) {
    const msg = message
      ? `Provider '${providerType}' configuration error: ${message}`
      : `Provider '${providerType}' configuration error`;
    super(msg);
    this.name = 'ProviderConfigError';
    this.providerType = providerType;
  }
}
```

6. Add `ProviderUnavailableError`:

```typescript
export class ProviderUnavailableError extends EmailError {
  public readonly providerType: string;

  constructor(providerType: string) {
    super(`Email provider '${providerType}' is unavailable`);
    this.name = 'ProviderUnavailableError';
    this.providerType = providerType;
  }
}
```

**Tests**: No tests needed for pure error class definitions.
**Commit**: `feat(email): add error classes for locale, version, attachment, batch, and provider`

---

### Task 11: Email Template Preview and Versioning Methods

**Files**:
- Modify: `packages/node/src/email/client.ts`
- Create: `packages/node/src/__tests__/email.test.ts`

**Steps**:

1. Add type imports to `client.ts`:

```typescript
import {
  // ... existing imports ...
  TemplatePreview,
  TemplateVersion,
  TemplateVersionListResponse,
} from './types';
import {
  // ... existing imports ...
  VersionNotFoundError,
} from './errors';
```

2. Add `previewTemplate` method after the template operations section:

```typescript
  // Template Preview Operations

  async previewTemplate(
    templateId: string,
    variables: Record<string, string>,
    locale?: string
  ): Promise<TemplatePreview> {
    try {
      return await this.request('POST', `/email/templates/${templateId}/preview`, {
        variables,
        locale,
      });
    } catch (error) {
      if (error instanceof TemplateNotFoundError) {
        throw new TemplateNotFoundError(templateId);
      }
      throw error;
    }
  }
```

3. Add `previewTemplateBySlug` method:

```typescript
  async previewTemplateBySlug(
    slug: string,
    variables: Record<string, string>,
    locale?: string
  ): Promise<TemplatePreview> {
    try {
      return await this.request('POST', `/email/templates/slug/${slug}/preview`, {
        variables,
        locale,
      });
    } catch (error) {
      if (error instanceof TemplateNotFoundError) {
        throw new TemplateNotFoundError(slug);
      }
      throw error;
    }
  }
```

4. Add `listTemplateVersions` method:

```typescript
  // Template Versioning Operations

  async listTemplateVersions(templateId: string): Promise<TemplateVersionListResponse> {
    try {
      return await this.request('GET', `/email/templates/${templateId}/versions`);
    } catch (error) {
      if (error instanceof TemplateNotFoundError) {
        throw new TemplateNotFoundError(templateId);
      }
      throw error;
    }
  }
```

5. Add `getTemplateVersion` method:

```typescript
  async getTemplateVersion(
    templateId: string,
    version: number
  ): Promise<TemplateVersion> {
    try {
      return await this.request(
        'GET',
        `/email/templates/${templateId}/versions/${version}`
      );
    } catch (error) {
      if (error instanceof TemplateNotFoundError) {
        throw new VersionNotFoundError(templateId, version);
      }
      throw error;
    }
  }
```

6. Add `revertToVersion` method:

```typescript
  async revertToVersion(
    templateId: string,
    version: number
  ): Promise<EmailTemplate> {
    try {
      return await this.request(
        'POST',
        `/email/templates/${templateId}/versions/${version}/revert`
      );
    } catch (error) {
      if (error instanceof TemplateNotFoundError) {
        throw new VersionNotFoundError(templateId, version);
      }
      throw error;
    }
  }
```

7. Create the test file `src/__tests__/email.test.ts` with a `global.fetch` mock pattern (same helper functions as settings test):

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { EmailClient } from '../email';
import {
  TemplateNotFoundError,
  VersionNotFoundError,
} from '../email/errors';

const mockFetch = vi.fn();
global.fetch = mockFetch;

function createJsonResponse(data: unknown, status = 200): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    statusText: 'OK',
    json: () => Promise.resolve(data),
    headers: new Headers(),
    redirected: false,
    type: 'basic',
    url: '',
    clone: () => ({} as Response),
    body: null,
    bodyUsed: false,
    arrayBuffer: () => Promise.resolve(new ArrayBuffer(0)),
    blob: () => Promise.resolve(new Blob()),
    formData: () => Promise.resolve(new FormData()),
    text: () => Promise.resolve(''),
  } as Response;
}

function createErrorResponse(status: number, data: unknown = {}): Response {
  return {
    ok: false,
    status,
    statusText: 'Error',
    json: () => Promise.resolve(data),
    headers: new Headers(),
    redirected: false,
    type: 'basic',
    url: '',
    clone: () => ({} as Response),
    body: null,
    bodyUsed: false,
    arrayBuffer: () => Promise.resolve(new ArrayBuffer(0)),
    blob: () => Promise.resolve(new Blob()),
    formData: () => Promise.resolve(new FormData()),
    text: () => Promise.resolve(''),
  } as Response;
}

describe('EmailClient', () => {
  let client: EmailClient;

  beforeEach(() => {
    vi.clearAllMocks();
    client = new EmailClient({
      baseUrl: 'https://api.example.com',
      accessToken: 'test-token',
    });
  });

  describe('previewTemplate', () => {
    it('should preview a template with variables', async () => {
      const mockPreview = {
        subject: 'Welcome, John!',
        html_content: '<h1>Welcome, John!</h1>',
        text_content: 'Welcome, John!',
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockPreview));

      const result = await client.previewTemplate('tpl-1', { name: 'John' });

      expect(result.subject).toBe('Welcome, John!');
      expect(result.html_content).toContain('John');
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/email/templates/tpl-1/preview'),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ variables: { name: 'John' }, locale: undefined }),
        })
      );
    });

    it('should pass locale for preview', async () => {
      mockFetch.mockResolvedValueOnce(
        createJsonResponse({ subject: 'Bienvenue!', html_content: '<h1>Bienvenue!</h1>' })
      );

      await client.previewTemplate('tpl-1', { name: 'Jean' }, 'fr');

      expect(mockFetch).toHaveBeenCalledWith(
        expect.anything(),
        expect.objectContaining({
          body: JSON.stringify({ variables: { name: 'Jean' }, locale: 'fr' }),
        })
      );
    });

    it('should throw TemplateNotFoundError on 404', async () => {
      mockFetch.mockResolvedValueOnce(createErrorResponse(404));

      await expect(client.previewTemplate('bad-id', {})).rejects.toThrow(TemplateNotFoundError);
    });
  });

  describe('previewTemplateBySlug', () => {
    it('should preview by slug', async () => {
      const mockPreview = {
        subject: 'Hello!',
        html_content: '<h1>Hello!</h1>',
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockPreview));

      const result = await client.previewTemplateBySlug('welcome-email', { name: 'Jane' });

      expect(result.subject).toBe('Hello!');
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/email/templates/slug/welcome-email/preview'),
        expect.anything()
      );
    });
  });

  describe('listTemplateVersions', () => {
    it('should list all versions of a template', async () => {
      const mockVersions = {
        data: [
          {
            version: 2,
            template_id: 'tpl-1',
            html_content: '<h1>v2</h1>',
            subject: 'v2 Subject',
            variables: ['name'],
            change_notes: 'Updated header',
            created_at: '2024-01-15T10:00:00Z',
            created_by: 'user-1',
          },
          {
            version: 1,
            template_id: 'tpl-1',
            html_content: '<h1>v1</h1>',
            subject: 'v1 Subject',
            variables: ['name'],
            created_at: '2024-01-10T10:00:00Z',
            created_by: 'user-1',
          },
        ],
        total: 2,
        page: 1,
        page_size: 20,
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockVersions));

      const result = await client.listTemplateVersions('tpl-1');

      expect(result.data).toHaveLength(2);
      expect(result.data[0].version).toBe(2);
      expect(result.data[1].version).toBe(1);
    });
  });

  describe('getTemplateVersion', () => {
    it('should get a specific version', async () => {
      const mockVersion = {
        version: 1,
        template_id: 'tpl-1',
        html_content: '<h1>v1</h1>',
        subject: 'v1 Subject',
        variables: ['name'],
        created_at: '2024-01-10T10:00:00Z',
        created_by: 'user-1',
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockVersion));

      const result = await client.getTemplateVersion('tpl-1', 1);

      expect(result.version).toBe(1);
      expect(result.template_id).toBe('tpl-1');
    });

    it('should throw VersionNotFoundError on 404', async () => {
      mockFetch.mockResolvedValueOnce(createErrorResponse(404));

      await expect(client.getTemplateVersion('tpl-1', 99)).rejects.toThrow(VersionNotFoundError);
    });
  });

  describe('revertToVersion', () => {
    it('should revert template to a previous version', async () => {
      const mockTemplate = {
        id: 'tpl-1',
        tenant_id: 'tenant-1',
        name: 'Welcome',
        slug: 'welcome-email',
        subject: 'v1 Subject',
        html_content: '<h1>v1</h1>',
        variables: ['name'],
        category: 'welcome',
        is_system: false,
        is_active: true,
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-15T10:00:00Z',
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockTemplate));

      const result = await client.revertToVersion('tpl-1', 1);

      expect(result.subject).toBe('v1 Subject');
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/email/templates/tpl-1/versions/1/revert'),
        expect.objectContaining({ method: 'POST' })
      );
    });
  });
});
```

**Tests**:
- `previewTemplate` sends POST with variables and optional locale
- `previewTemplate` throws `TemplateNotFoundError` on 404
- `previewTemplateBySlug` uses correct slug URL path
- `listTemplateVersions` returns paginated version list
- `getTemplateVersion` returns a specific version
- `getTemplateVersion` throws `VersionNotFoundError` on 404
- `revertToVersion` sends POST to revert endpoint

**Commit**: `feat(email): add template preview and versioning methods`

---

### Task 12: Email Locale/i18n Methods

**Files**:
- Modify: `packages/node/src/email/client.ts`
- Modify: `packages/node/src/__tests__/email.test.ts`

**Steps**:

1. Add type imports to `client.ts`:

```typescript
import {
  // ... existing imports ...
  TemplateLocale,
  TemplateLocaleListResponse,
  SetTemplateLocaleRequest,
} from './types';
import {
  // ... existing imports ...
  LocaleNotFoundError,
} from './errors';
```

2. Add `setTemplateLocale` method:

```typescript
  // Locale/i18n Operations

  async setTemplateLocale(
    templateId: string,
    locale: string,
    content: SetTemplateLocaleRequest
  ): Promise<TemplateLocale> {
    try {
      return await this.request(
        'PUT',
        `/email/templates/${templateId}/locales/${locale}`,
        content
      );
    } catch (error) {
      if (error instanceof TemplateNotFoundError) {
        throw new TemplateNotFoundError(templateId);
      }
      throw error;
    }
  }
```

3. Add `getTemplateLocale` method:

```typescript
  async getTemplateLocale(
    templateId: string,
    locale: string
  ): Promise<TemplateLocale> {
    try {
      return await this.request(
        'GET',
        `/email/templates/${templateId}/locales/${locale}`
      );
    } catch (error) {
      if (error instanceof TemplateNotFoundError) {
        throw new LocaleNotFoundError(templateId, locale);
      }
      throw error;
    }
  }
```

4. Add `listTemplateLocales` method:

```typescript
  async listTemplateLocales(
    templateId: string
  ): Promise<TemplateLocaleListResponse> {
    try {
      return await this.request('GET', `/email/templates/${templateId}/locales`);
    } catch (error) {
      if (error instanceof TemplateNotFoundError) {
        throw new TemplateNotFoundError(templateId);
      }
      throw error;
    }
  }
```

5. Add `deleteTemplateLocale` method:

```typescript
  async deleteTemplateLocale(
    templateId: string,
    locale: string
  ): Promise<void> {
    try {
      await this.request(
        'DELETE',
        `/email/templates/${templateId}/locales/${locale}`
      );
    } catch (error) {
      if (error instanceof TemplateNotFoundError) {
        throw new LocaleNotFoundError(templateId, locale);
      }
      throw error;
    }
  }
```

6. Add tests to `email.test.ts`:

```typescript
  describe('setTemplateLocale', () => {
    it('should set locale content for a template', async () => {
      const mockLocale = {
        template_id: 'tpl-1',
        locale: 'fr',
        subject: 'Bienvenue, {{name}}!',
        html_content: '<h1>Bienvenue, {{name}}!</h1>',
        text_content: 'Bienvenue, {{name}}!',
        variables: ['name'],
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockLocale));

      const result = await client.setTemplateLocale('tpl-1', 'fr', {
        subject: 'Bienvenue, {{name}}!',
        html_content: '<h1>Bienvenue, {{name}}!</h1>',
        text_content: 'Bienvenue, {{name}}!',
      });

      expect(result.locale).toBe('fr');
      expect(result.subject).toContain('Bienvenue');
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/email/templates/tpl-1/locales/fr'),
        expect.objectContaining({ method: 'PUT' })
      );
    });

    it('should throw TemplateNotFoundError on 404', async () => {
      mockFetch.mockResolvedValueOnce(createErrorResponse(404));

      await expect(
        client.setTemplateLocale('bad-id', 'fr', {
          subject: 'Test',
          html_content: '<p>Test</p>',
        })
      ).rejects.toThrow(TemplateNotFoundError);
    });
  });

  describe('getTemplateLocale', () => {
    it('should get locale content', async () => {
      const mockLocale = {
        template_id: 'tpl-1',
        locale: 'es',
        subject: 'Bienvenido!',
        html_content: '<h1>Bienvenido!</h1>',
        variables: ['name'],
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockLocale));

      const result = await client.getTemplateLocale('tpl-1', 'es');

      expect(result.locale).toBe('es');
      expect(result.subject).toBe('Bienvenido!');
    });

    it('should throw LocaleNotFoundError on 404', async () => {
      mockFetch.mockResolvedValueOnce(createErrorResponse(404));

      await expect(client.getTemplateLocale('tpl-1', 'zz')).rejects.toThrow(
        LocaleNotFoundError
      );
    });
  });

  describe('listTemplateLocales', () => {
    it('should list all locales for a template', async () => {
      const mockResponse = {
        data: [
          { template_id: 'tpl-1', locale: 'en', subject: 'Welcome!', html_content: '<h1>Welcome!</h1>', variables: ['name'] },
          { template_id: 'tpl-1', locale: 'fr', subject: 'Bienvenue!', html_content: '<h1>Bienvenue!</h1>', variables: ['name'] },
        ],
        total: 2,
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockResponse));

      const result = await client.listTemplateLocales('tpl-1');

      expect(result.data).toHaveLength(2);
      expect(result.total).toBe(2);
    });
  });

  describe('deleteTemplateLocale', () => {
    it('should delete a locale', async () => {
      mockFetch.mockResolvedValueOnce(createJsonResponse(undefined, 204));

      await client.deleteTemplateLocale('tpl-1', 'fr');

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/email/templates/tpl-1/locales/fr'),
        expect.objectContaining({ method: 'DELETE' })
      );
    });

    it('should throw LocaleNotFoundError on 404', async () => {
      mockFetch.mockResolvedValueOnce(createErrorResponse(404));

      await expect(client.deleteTemplateLocale('tpl-1', 'zz')).rejects.toThrow(
        LocaleNotFoundError
      );
    });
  });
```

**Tests**:
- `setTemplateLocale` sends PUT with locale content
- `setTemplateLocale` throws `TemplateNotFoundError` on 404
- `getTemplateLocale` retrieves specific locale
- `getTemplateLocale` throws `LocaleNotFoundError` on 404
- `listTemplateLocales` returns list with total
- `deleteTemplateLocale` sends DELETE
- `deleteTemplateLocale` throws `LocaleNotFoundError` on 404

**Commit**: `feat(email): add template locale/i18n methods`

---

### Task 13: Email Provider Management Methods

**Files**:
- Modify: `packages/node/src/email/client.ts`
- Modify: `packages/node/src/__tests__/email.test.ts`

**Steps**:

1. Add type imports to `client.ts`:

```typescript
import {
  // ... existing imports ...
  EmailProvider,
  EmailProviderListResponse,
  ConfigureProviderRequest,
} from './types';
import {
  // ... existing imports ...
  ProviderConfigError,
  ProviderUnavailableError,
} from './errors';
```

2. Update `request` method to handle 422 status for provider config errors, adding after the 503 check:

```typescript
        if (status === 422) {
          let errorData: Record<string, unknown> = {};
          try {
            errorData = await response.json();
          } catch {
            // ignore
          }
          const providerType = (errorData.provider_type as string) || 'unknown';
          const message = (errorData.message as string) || undefined;
          throw new ProviderConfigError(providerType, message);
        }
```

Note: The 422 error body parsing is needed because the existing `request` method already parses error data before the status checks. Adjust the parsing to use the already-parsed `data` variable:

```typescript
        if (status === 422) {
          const providerType = (data.provider_type as string) || 'unknown';
          const message = (data.message as string) || undefined;
          throw new ProviderConfigError(providerType, message);
        }
```

3. Add `listProviders` method:

```typescript
  // Provider Operations

  async listProviders(): Promise<EmailProviderListResponse> {
    return this.request('GET', '/email/providers');
  }
```

4. Add `configureProvider` method:

```typescript
  async configureProvider(
    config: ConfigureProviderRequest
  ): Promise<EmailProvider> {
    return this.request('POST', '/email/providers', config);
  }
```

5. Add tests:

```typescript
  describe('listProviders', () => {
    it('should list all configured email providers', async () => {
      const mockResponse = {
        data: [
          {
            type: 'sendgrid',
            config: { api_key: '***' },
            is_active: true,
            verified_at: '2024-01-10T10:00:00Z',
          },
          {
            type: 'smtp',
            config: { host: 'smtp.example.com', port: 587 },
            is_active: false,
            verified_at: null,
          },
        ],
        total: 2,
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockResponse));

      const result = await client.listProviders();

      expect(result.data).toHaveLength(2);
      expect(result.data[0].type).toBe('sendgrid');
      expect(result.data[0].is_active).toBe(true);
    });
  });

  describe('configureProvider', () => {
    it('should configure a new email provider', async () => {
      const mockProvider = {
        type: 'sendgrid',
        config: { api_key: 'SG.xxxxx' },
        is_active: true,
        verified_at: null,
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockProvider));

      const result = await client.configureProvider({
        type: 'sendgrid',
        config: { api_key: 'SG.xxxxx' },
        is_active: true,
      });

      expect(result.type).toBe('sendgrid');
      expect(result.is_active).toBe(true);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/email/providers'),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({
            type: 'sendgrid',
            config: { api_key: 'SG.xxxxx' },
            is_active: true,
          }),
        })
      );
    });

    it('should throw ProviderConfigError on 422', async () => {
      mockFetch.mockResolvedValueOnce(
        createErrorResponse(422, {
          provider_type: 'ses',
          message: 'Invalid AWS credentials',
        })
      );

      await expect(
        client.configureProvider({
          type: 'ses',
          config: { access_key: 'bad' },
        })
      ).rejects.toThrow(ProviderConfigError);
    });
  });
```

**Tests**:
- `listProviders` returns list of configured providers
- `configureProvider` sends POST with provider config
- `configureProvider` throws `ProviderConfigError` on 422

**Commit**: `feat(email): add provider management methods`

---

### Task 14: Email Batch Send and Send History Methods

**Files**:
- Modify: `packages/node/src/email/client.ts`
- Modify: `packages/node/src/__tests__/email.test.ts`

**Steps**:

1. Add type imports to `client.ts`:

```typescript
import {
  // ... existing imports ...
  BatchSendRequest,
  BatchSendResult,
  EmailSendDetails,
  SendHistoryListResponse,
  ListSendHistoryParams,
} from './types';
import {
  // ... existing imports ...
  BatchTooLargeError,
} from './errors';
```

2. Update `request` method to handle 413 (Payload Too Large) for batch limits, adding after 422:

```typescript
        if (status === 413) {
          const recipientCount = (data.recipient_count as number) || 0;
          const maxRecipients = (data.max_recipients as number) || 0;
          throw new BatchTooLargeError(recipientCount, maxRecipients);
        }
```

3. Add `sendBatch` method:

```typescript
  // Batch Send Operations

  async sendBatch(request: BatchSendRequest): Promise<BatchSendResult> {
    return this.request('POST', '/email/send-batch', request);
  }
```

4. Add `listSendHistory` method:

```typescript
  // Send History Operations

  async listSendHistory(
    params?: ListSendHistoryParams
  ): Promise<SendHistoryListResponse> {
    return this.request(
      'GET',
      '/email/history',
      undefined,
      params as Record<string, unknown>
    );
  }
```

5. Add `getSendDetails` method:

```typescript
  async getSendDetails(messageId: string): Promise<EmailSendDetails> {
    return this.request('GET', `/email/history/${messageId}`);
  }
```

6. Add tests:

```typescript
  describe('sendBatch', () => {
    it('should send batch emails', async () => {
      const mockResult = {
        batch_id: 'batch-1',
        total: 3,
        sent_count: 2,
        failed_count: 1,
        failures: [
          { to: 'bad@example.com', error: 'Invalid email address' },
        ],
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockResult));

      const result = await client.sendBatch({
        template_slug: 'welcome-email',
        recipients: [
          { to: 'user1@example.com', variables: { name: 'User 1' } },
          { to: 'user2@example.com', variables: { name: 'User 2' } },
          { to: 'bad@example.com', variables: { name: 'Bad' } },
        ],
      });

      expect(result.batch_id).toBe('batch-1');
      expect(result.sent_count).toBe(2);
      expect(result.failed_count).toBe(1);
      expect(result.failures).toHaveLength(1);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/email/send-batch'),
        expect.objectContaining({ method: 'POST' })
      );
    });

    it('should throw BatchTooLargeError on 413', async () => {
      mockFetch.mockResolvedValueOnce(
        createErrorResponse(413, {
          recipient_count: 1001,
          max_recipients: 1000,
        })
      );

      await expect(
        client.sendBatch({
          template_slug: 'bulk',
          recipients: Array(1001).fill({ to: 'a@b.com', variables: {} }),
        })
      ).rejects.toThrow(BatchTooLargeError);
    });
  });

  describe('listSendHistory', () => {
    it('should list send history with filters', async () => {
      const mockResponse = {
        data: [
          {
            message_id: 'msg-1',
            template_slug: 'welcome-email',
            to: ['user@example.com'],
            subject: 'Welcome!',
            status: 'delivered',
            provider: 'sendgrid',
            sent_at: '2024-01-15T10:00:00Z',
            delivered_at: '2024-01-15T10:01:00Z',
          },
        ],
        total: 1,
        page: 1,
        page_size: 20,
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockResponse));

      const result = await client.listSendHistory({
        status: 'delivered',
        page: 1,
        page_size: 20,
      });

      expect(result.data).toHaveLength(1);
      expect(result.data[0].status).toBe('delivered');

      const calledUrl = mockFetch.mock.calls[0][0] as string;
      expect(calledUrl).toContain('status=delivered');
    });
  });

  describe('getSendDetails', () => {
    it('should get send details with delivery events', async () => {
      const mockDetails = {
        record: {
          message_id: 'msg-1',
          template_slug: 'welcome-email',
          to: ['user@example.com'],
          subject: 'Welcome!',
          status: 'delivered',
          provider: 'sendgrid',
          sent_at: '2024-01-15T10:00:00Z',
          delivered_at: '2024-01-15T10:01:00Z',
        },
        events: [
          {
            message_id: 'msg-1',
            event_type: 'delivered',
            timestamp: '2024-01-15T10:01:00Z',
          },
          {
            message_id: 'msg-1',
            event_type: 'opened',
            timestamp: '2024-01-15T11:00:00Z',
          },
        ],
      };

      mockFetch.mockResolvedValueOnce(createJsonResponse(mockDetails));

      const result = await client.getSendDetails('msg-1');

      expect(result.record.message_id).toBe('msg-1');
      expect(result.events).toHaveLength(2);
      expect(result.events[0].event_type).toBe('delivered');
      expect(result.events[1].event_type).toBe('opened');
    });
  });
```

**Tests**:
- `sendBatch` sends POST with recipients and returns batch result with failures
- `sendBatch` throws `BatchTooLargeError` on 413
- `listSendHistory` passes filter params as query string
- `getSendDetails` returns record with delivery events

**Commit**: `feat(email): add batch send, send history, and delivery tracking methods`

---

### Task 15: Email Module Index Exports Update

**Files**:
- Modify: `packages/node/src/email/index.ts`
- Modify: `packages/node/src/index.ts`

**Steps**:

1. The `email/index.ts` already re-exports `* from './types'` and `* from './errors'`, so all new types and errors are automatically exported. No changes needed to `email/index.ts`.

2. Update the email type exports in `src/index.ts` to include the new types:

```typescript
export type {
  // Email types
  EmailTemplate,
  EmailConfig,
  SendEmailRequest,
  SendTemplateRequest,
  EmailSendResult,
  CreateTemplateRequest,
  UpdateTemplateRequest,
  TemplateListResponse,
  TemplateCategory,
  // New types
  ProviderType,
  EmailSendStatus,
  DeliveryEventType,
  TemplateVersion,
  TemplateVersionListResponse,
  TemplateLocale,
  TemplateLocaleListResponse,
  TemplatePreview,
  SetTemplateLocaleRequest,
  Attachment,
  BatchRecipient,
  BatchSendRequest,
  BatchSendFailure,
  BatchSendResult,
  EmailSendRecord,
  SendHistoryListResponse,
  ListSendHistoryParams,
  EmailDeliveryEvent,
  EmailSendDetails,
  EmailProvider,
  ConfigureProviderRequest,
  EmailProviderListResponse,
} from './email';
```

**Tests**: Run `npm run typecheck` to ensure all exports resolve correctly.
**Commit**: `feat(email): update module exports for all new email types`

---

### Task 16: Final Validation and Integration Test Run

**Files**:
- All modified files from Tasks 1-15

**Steps**:

1. Run the TypeScript type checker to validate all new types and method signatures:

```bash
cd packages/node && npm run typecheck
```

2. Run the full test suite:

```bash
cd packages/node && npm test
```

3. Verify the build succeeds:

```bash
cd packages/node && npm run build
```

4. Verify all new exports are accessible by checking the built output includes the new types.

5. Fix any type errors, missing imports, or test failures discovered during validation.

**Tests**: All existing tests must pass. All new tests in `settings.test.ts` and `email.test.ts` must pass.
**Commit**: `test(settings,email): verify all tests pass for enhanced settings and email modules`

---

## Summary

| Area | Tasks | New Types | New Methods | New Errors | Test File |
|------|-------|-----------|-------------|------------|-----------|
| Settings | Tasks 1-8 | 19 interfaces/types | 10 methods | 4 error classes | `src/__tests__/settings.test.ts` |
| Email | Tasks 9-15 | 22 interfaces/types | 12 methods | 6 error classes | `src/__tests__/email.test.ts` |
| Validation | Task 16 | - | - | - | All test files |

### Settings Methods Added (10)

| Method | HTTP | Endpoint |
|--------|------|----------|
| `getSettingHistory(key, page?, pageSize?)` | GET | `/settings/{key}/history` |
| `getCategoryHistory(category, page?, pageSize?)` | GET | `/settings/{category}/history` |
| `getForEnvironment(key, environment)` | GET | `/settings/{key}/environments/{env}` |
| `setForEnvironment(key, value, environment)` | PUT | `/settings/{key}/environments/{env}` |
| `listEnvironmentOverrides(environment)` | GET | `/settings/environments/{env}` |
| `exportSettings(format?, categories?)` | GET | `/settings/export` |
| `importSettings(data, format?, strategy?)` | POST | `/settings/import` |
| `lockSetting(key, reason?)` | POST | `/settings/{key}/lock` |
| `unlockSetting(key)` | POST | `/settings/{key}/unlock` |
| `listLockedSettings()` | GET | `/settings/locked` |
| `getEffectiveSetting(key, userId?, environment?)` | GET | `/settings/{key}/effective` |
| `getSensitiveValue(key)` | GET | `/settings/{key}/sensitive` |
| `bulkUpdate(settings)` | PUT | `/settings/bulk` |

### Email Methods Added (12)

| Method | HTTP | Endpoint |
|--------|------|----------|
| `previewTemplate(templateId, variables, locale?)` | POST | `/email/templates/{id}/preview` |
| `previewTemplateBySlug(slug, variables, locale?)` | POST | `/email/templates/slug/{slug}/preview` |
| `listTemplateVersions(templateId)` | GET | `/email/templates/{id}/versions` |
| `getTemplateVersion(templateId, version)` | GET | `/email/templates/{id}/versions/{v}` |
| `revertToVersion(templateId, version)` | POST | `/email/templates/{id}/versions/{v}/revert` |
| `setTemplateLocale(templateId, locale, content)` | PUT | `/email/templates/{id}/locales/{locale}` |
| `getTemplateLocale(templateId, locale)` | GET | `/email/templates/{id}/locales/{locale}` |
| `listTemplateLocales(templateId)` | GET | `/email/templates/{id}/locales` |
| `deleteTemplateLocale(templateId, locale)` | DELETE | `/email/templates/{id}/locales/{locale}` |
| `listProviders()` | GET | `/email/providers` |
| `configureProvider(config)` | POST | `/email/providers` |
| `sendBatch(request)` | POST | `/email/send-batch` |
| `listSendHistory(params?)` | GET | `/email/history` |
| `getSendDetails(messageId)` | GET | `/email/history/{messageId}` |

### New HTTP Status Code Mappings

| Status | Settings Error | Email Error |
|--------|---------------|-------------|
| 403 | `ReadonlySettingError` (when `code === 'readonly_setting'`) | - |
| 413 | - | `BatchTooLargeError` |
| 422 | `ImportValidationError` | `ProviderConfigError` |
| 423 | `SettingLockedError` | - |

### Estimated Time

- Tasks 1-2 (Settings types/errors): ~15 minutes
- Tasks 3-7 (Settings methods + tests): ~45 minutes
- Task 8 (Settings exports): ~5 minutes
- Tasks 9-10 (Email types/errors): ~15 minutes
- Tasks 11-14 (Email methods + tests): ~50 minutes
- Task 15 (Email exports): ~5 minutes
- Task 16 (Validation): ~10 minutes
- **Total: ~2.5 hours**
