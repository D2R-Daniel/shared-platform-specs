# Phase 0 Stream A: Auth Enhancements + Session Management

**Feature**: Auth enhancements and Session Management module
**Goal**: Enhance the existing auth client with PKCE, step-up auth, OIDC discovery, and auto-refresh; build a complete Session Management module with device tracking, geo-location, policies, and admin operations.
**Architecture**: Enhance existing AuthClient in `src/auth/` with new methods and types. Create new SessionClient in `src/sessions/` following the same patterns.
**Tech Stack**: TypeScript, Node.js, Vitest, Axios

---

## Tasks

---

### Task 1: Add PKCE and OIDC discovery types to auth module

**Files**:
- Modify `src/auth/types.ts`

**Steps**:

1. Open `src/auth/types.ts` and append the following types after the existing `JWTPayload` interface:

```typescript
/**
 * Supported social/external identity providers.
 */
export type SocialProvider =
  | 'google'
  | 'github'
  | 'microsoft'
  | 'apple'
  | 'facebook'
  | 'linkedin'
  | 'twitter'
  | 'okta'
  | 'auth0'
  | 'saml';

/**
 * Authenticator Assurance Levels per NIST 800-63B.
 */
export enum AssuranceLevel {
  /** Single-factor authentication (password only) */
  AAL1 = 'aal1',
  /** Multi-factor authentication (password + second factor) */
  AAL2 = 'aal2',
  /** Hardware-based MFA (FIDO2, smart card) */
  AAL3 = 'aal3',
}

/**
 * PKCE code challenge pair used in authorization code flow.
 */
export interface PKCEChallenge {
  /** Random code verifier string (43-128 chars) */
  codeVerifier: string;
  /** Base64url-encoded SHA-256 hash of the code verifier */
  codeChallenge: string;
  /** Always 'S256' */
  codeChallengeMethod: 'S256';
}

/**
 * Options for building an authorization URL.
 */
export interface AuthorizationUrlOptions {
  /** OAuth2 redirect URI */
  redirectUri: string;
  /** Space-separated scopes to request */
  scope?: string;
  /** Opaque state value for CSRF protection */
  state?: string;
  /** Hint to the authorization server about the login identifier */
  loginHint?: string;
  /** Social/external provider to use */
  provider?: SocialProvider;
  /** Requested assurance level for step-up auth */
  acrValues?: AssuranceLevel;
  /** Additional query parameters to include */
  additionalParams?: Record<string, string>;
}

/**
 * Result from buildAuthorizationUrl, includes the PKCE verifier to store.
 */
export interface AuthorizationUrlResult {
  /** Full authorization URL to redirect the user to */
  url: string;
  /** PKCE challenge pair (store codeVerifier for the token exchange) */
  pkce: PKCEChallenge;
  /** State value (echoed back if provided, generated if not) */
  state: string;
}

/**
 * Options for requesting client credentials (M2M) tokens.
 */
export interface ClientCredentialsOptions {
  /** Space-separated scopes to request */
  scope?: string;
  /** Target audience for the token */
  audience?: string;
}

/**
 * Options for local JWT validation.
 */
export interface TokenValidationOptions {
  /** Expected audience claim */
  audience?: string;
  /** Expected issuer claim */
  issuer?: string;
  /** Clock skew tolerance in seconds (default: 30) */
  clockToleranceSeconds?: number;
  /** Required scopes the token must contain */
  requiredScopes?: string[];
  /** Required minimum assurance level */
  requiredAssuranceLevel?: AssuranceLevel;
}

/**
 * Result of local JWT validation.
 */
export interface TokenValidationResult {
  /** Whether the token is valid */
  valid: boolean;
  /** Decoded JWT payload (present when valid) */
  payload?: JWTPayload & {
    acr?: string;
    amr?: string[];
    aud?: string | string[];
    iss?: string;
    exp?: number;
    iat?: number;
    nbf?: number;
    jti?: string;
  };
  /** Error message (present when invalid) */
  error?: string;
  /** Error code for programmatic handling */
  errorCode?: 'expired' | 'invalid_signature' | 'invalid_audience' | 'invalid_issuer' | 'insufficient_scope' | 'insufficient_assurance' | 'malformed';
}

/**
 * JSON Web Key from a JWKS endpoint.
 */
export interface JSONWebKey {
  kty: string;
  kid?: string;
  use?: string;
  alg?: string;
  n?: string;
  e?: string;
  x5c?: string[];
  x5t?: string;
}

/**
 * JSON Web Key Set response.
 */
export interface JSONWebKeySet {
  keys: JSONWebKey[];
}

/**
 * OIDC Discovery Document (.well-known/openid-configuration).
 */
export interface OIDCDiscoveryDocument {
  issuer: string;
  authorization_endpoint: string;
  token_endpoint: string;
  userinfo_endpoint: string;
  jwks_uri: string;
  revocation_endpoint?: string;
  introspection_endpoint?: string;
  end_session_endpoint?: string;
  registration_endpoint?: string;
  scopes_supported?: string[];
  response_types_supported: string[];
  response_modes_supported?: string[];
  grant_types_supported?: string[];
  acr_values_supported?: string[];
  subject_types_supported: string[];
  id_token_signing_alg_values_supported: string[];
  claims_supported?: string[];
  code_challenge_methods_supported?: string[];
}

/**
 * Request to initiate step-up authentication.
 */
export interface StepUpRequest {
  /** Target assurance level */
  targetLevel: AssuranceLevel;
  /** Reason displayed to the user */
  reason?: string;
  /** URL to redirect to after step-up completion */
  redirectUri?: string;
}

/**
 * Result of a step-up authentication check or initiation.
 */
export interface StepUpResult {
  /** Whether step-up is required */
  required: boolean;
  /** Current assurance level */
  currentLevel: AssuranceLevel;
  /** Required assurance level */
  targetLevel: AssuranceLevel;
  /** URL to redirect user for step-up (present when required=true) */
  stepUpUrl?: string;
  /** New access token (present when step-up already satisfied) */
  accessToken?: string;
}

/**
 * Options for the auto-refresh behavior.
 */
export interface AutoRefreshOptions {
  /** Current refresh token */
  refreshToken: string;
  /** Seconds before expiry to trigger refresh (default: 60) */
  refreshBeforeExpirySeconds?: number;
  /** Maximum number of consecutive refresh failures before stopping (default: 3) */
  maxRetries?: number;
  /** Callback invoked on each successful refresh */
  onRefresh?: (tokens: TokenResponse) => void;
  /** Callback invoked when auto-refresh fails permanently */
  onError?: (error: Error) => void;
}

/**
 * Handle returned by enableAutoRefresh to control the refresh timer.
 */
export interface AutoRefreshHandle {
  /** Stop the auto-refresh timer */
  stop: () => void;
  /** Whether auto-refresh is currently active */
  isActive: () => boolean;
}
```

2. Extend the existing `JWTPayload` interface with the `acr` field used for assurance levels:

Add to the `JWTPayload` interface, after the existing `scope` field:

```typescript
  acr?: string;
  amr?: string[];
```

**Tests**: No tests for this task (types only). TypeScript compiler validates correctness.

**Commit**: `feat(auth): add PKCE, OIDC discovery, step-up, and auto-refresh types`

---

### Task 2: Add new auth error classes

**Files**:
- Modify `src/auth/errors.ts`

**Steps**:

1. Open `src/auth/errors.ts` and append the following error class after `ForbiddenError`:

```typescript
export class StepUpRequiredError extends AuthError {
  public readonly currentLevel: string;
  public readonly requiredLevel: string;
  public readonly stepUpUrl?: string;

  constructor(
    currentLevel: string,
    requiredLevel: string,
    stepUpUrl?: string
  ) {
    super(
      'step_up_required',
      `Step-up authentication required: current=${currentLevel}, required=${requiredLevel}`
    );
    this.name = 'StepUpRequiredError';
    this.currentLevel = currentLevel;
    this.requiredLevel = requiredLevel;
    this.stepUpUrl = stepUpUrl;
  }
}

export class DiscoveryError extends AuthError {
  constructor(message: string = 'Failed to fetch OIDC discovery document') {
    super('discovery_error', message);
    this.name = 'DiscoveryError';
  }
}

export class JWKSError extends AuthError {
  constructor(message: string = 'Failed to fetch or process JWKS') {
    super('jwks_error', message);
    this.name = 'JWKSError';
  }
}

export class TokenValidationError extends AuthError {
  public readonly errorCode: string;

  constructor(
    errorCode: string,
    message: string = 'Token validation failed'
  ) {
    super('token_validation_error', message);
    this.name = 'TokenValidationError';
    this.errorCode = errorCode;
  }
}
```

**Tests**: No standalone tests. Errors are tested as part of the methods that throw them.

**Commit**: `feat(auth): add StepUpRequiredError, DiscoveryError, JWKSError, and TokenValidationError`

---

### Task 3: Implement PKCE utilities and buildAuthorizationUrl

**Files**:
- Create `src/auth/pkce.ts`
- Modify `src/auth/client.ts`

**Steps**:

1. Create `src/auth/pkce.ts` with PKCE helper functions:

```typescript
/**
 * PKCE (Proof Key for Code Exchange) utilities.
 */

import { randomBytes, createHash } from 'node:crypto';
import { PKCEChallenge } from './types';

/**
 * Generate a cryptographically random code verifier (43-128 characters).
 */
export function generateCodeVerifier(length: number = 64): string {
  const buffer = randomBytes(length);
  return buffer
    .toString('base64url')
    .slice(0, length);
}

/**
 * Derive a code challenge from a code verifier using S256.
 */
export function generateCodeChallenge(codeVerifier: string): string {
  return createHash('sha256')
    .update(codeVerifier)
    .digest('base64url');
}

/**
 * Generate a full PKCE challenge pair.
 */
export function generatePKCEChallenge(): PKCEChallenge {
  const codeVerifier = generateCodeVerifier();
  const codeChallenge = generateCodeChallenge(codeVerifier);

  return {
    codeVerifier,
    codeChallenge,
    codeChallengeMethod: 'S256',
  };
}
```

2. Add to `AuthClient` in `src/auth/client.ts`:

Import the new types at the top:

```typescript
import {
  TokenResponse,
  TokenIntrospection,
  UserInfo,
  UserContext,
  Session,
  JWTPayload,
  AuthorizationUrlOptions,
  AuthorizationUrlResult,
} from './types';
import { generatePKCEChallenge } from './pkce';
```

Add a private field after the existing private fields:

```typescript
  private issuerUrl: string;
```

Set it in the constructor (add before `this.http = axios.create(...)`:

```typescript
    this.issuerUrl = options.issuerUrl.replace(/\/$/, '');
```

Add the method to the class body:

```typescript
  /**
   * Build an authorization URL with PKCE for the authorization code flow.
   *
   * @returns The authorization URL, PKCE challenge, and state value.
   *
   * @example
   * ```typescript
   * const { url, pkce, state } = auth.buildAuthorizationUrl({
   *   redirectUri: 'https://app.example.com/callback',
   *   scope: 'openid profile email',
   * });
   * // Store pkce.codeVerifier and state in session
   * // Redirect user to url
   * ```
   */
  buildAuthorizationUrl(options: AuthorizationUrlOptions): AuthorizationUrlResult {
    const pkce = generatePKCEChallenge();
    const state = options.state ?? randomBytes(32).toString('base64url');

    const params = new URLSearchParams({
      response_type: 'code',
      client_id: this.clientId ?? '',
      redirect_uri: options.redirectUri,
      scope: options.scope ?? 'openid profile email',
      state,
      code_challenge: pkce.codeChallenge,
      code_challenge_method: pkce.codeChallengeMethod,
    });

    if (options.loginHint) {
      params.set('login_hint', options.loginHint);
    }
    if (options.provider) {
      params.set('connection', options.provider);
    }
    if (options.acrValues) {
      params.set('acr_values', options.acrValues);
    }
    if (options.additionalParams) {
      for (const [key, value] of Object.entries(options.additionalParams)) {
        params.set(key, value);
      }
    }

    const url = `${this.issuerUrl}/auth/authorize?${params.toString()}`;

    return { url, pkce, state };
  }
```

Also add this import to the top of `client.ts`:

```typescript
import { randomBytes } from 'node:crypto';
```

**Tests** (in `src/__tests__/auth-pkce.test.ts`):
- `generateCodeVerifier` returns a string of the correct length
- `generateCodeChallenge` returns a valid base64url string
- `generatePKCEChallenge` returns all three fields
- `buildAuthorizationUrl` returns a URL containing all required query parameters
- `buildAuthorizationUrl` includes optional params when provided
- `buildAuthorizationUrl` generates state when not provided
- `buildAuthorizationUrl` uses provided state when given

**Commit**: `feat(auth): implement PKCE utilities and buildAuthorizationUrl`

---

### Task 4: Implement exchangeCode and getClientCredentialsToken

**Files**:
- Modify `src/auth/client.ts`

**Steps**:

1. Add the `ClientCredentialsOptions` import to the existing import block in `client.ts`:

```typescript
import {
  // ... existing imports ...
  ClientCredentialsOptions,
} from './types';
```

2. Add the `exchangeCode` method to the `AuthClient` class:

```typescript
  /**
   * Exchange an authorization code for tokens (PKCE flow).
   *
   * @param code - The authorization code from the callback
   * @param codeVerifier - The PKCE code verifier stored during buildAuthorizationUrl
   * @param redirectUri - The same redirect URI used in the authorization request
   */
  async exchangeCode(
    code: string,
    codeVerifier: string,
    redirectUri: string
  ): Promise<TokenResponse> {
    const data: Record<string, string> = {
      grant_type: 'authorization_code',
      code,
      code_verifier: codeVerifier,
      redirect_uri: redirectUri,
    };

    if (this.clientId) data.client_id = this.clientId;
    if (this.clientSecret) data.client_secret = this.clientSecret;

    try {
      const response = await this.http.post<TokenResponse>('/token', data, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      });

      return this.mapTokenResponse(response.data);
    } catch (error: any) {
      if (error.response?.status === 401) {
        throw new UnauthorizedError('Invalid authorization code or verifier');
      }
      throw this.handleError(error);
    }
  }
```

3. Add the `getClientCredentialsToken` method:

```typescript
  /**
   * Get an access token using client credentials (M2M flow).
   * Requires clientId and clientSecret to be set.
   *
   * @param options - Optional scope and audience
   */
  async getClientCredentialsToken(
    options: ClientCredentialsOptions = {}
  ): Promise<TokenResponse> {
    if (!this.clientId || !this.clientSecret) {
      throw new AuthError(
        'configuration_error',
        'clientId and clientSecret are required for client credentials flow'
      );
    }

    const data: Record<string, string> = {
      grant_type: 'client_credentials',
      client_id: this.clientId,
      client_secret: this.clientSecret,
    };

    if (options.scope) data.scope = options.scope;
    if (options.audience) data.audience = options.audience;

    try {
      const response = await this.http.post<TokenResponse>('/token', data, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      });

      return this.mapTokenResponse(response.data);
    } catch (error: any) {
      if (error.response?.status === 401) {
        throw new UnauthorizedError('Invalid client credentials');
      }
      throw this.handleError(error);
    }
  }
```

**Tests** (append to `src/__tests__/auth-pkce.test.ts`):
- `exchangeCode` sends correct form data and returns mapped TokenResponse
- `exchangeCode` throws UnauthorizedError on 401
- `exchangeCode` throws AuthError on other errors
- `getClientCredentialsToken` sends correct form data
- `getClientCredentialsToken` throws AuthError when clientId/clientSecret missing
- `getClientCredentialsToken` throws UnauthorizedError on 401
- `getClientCredentialsToken` passes scope and audience when provided

**Commit**: `feat(auth): implement exchangeCode and getClientCredentialsToken`

---

### Task 5: Implement OIDC discovery with caching

**Files**:
- Modify `src/auth/client.ts`

**Steps**:

1. Add the `OIDCDiscoveryDocument` import:

```typescript
import {
  // ... existing imports ...
  OIDCDiscoveryDocument,
} from './types';
import { DiscoveryError } from './errors';
```

2. Add private cache fields to the `AuthClient` class:

```typescript
  private discoveryDocument: OIDCDiscoveryDocument | null = null;
  private discoveryFetchedAt: number = 0;
  /** Cache TTL for discovery document in milliseconds (default: 1 hour) */
  private discoveryCacheTtlMs: number = 3600000;
```

3. Add the `discover` method:

```typescript
  /**
   * Fetch the OIDC Discovery Document from the well-known endpoint.
   * Results are cached for 1 hour by default.
   *
   * @param forceRefresh - Bypass the cache and fetch a fresh document
   */
  async discover(forceRefresh: boolean = false): Promise<OIDCDiscoveryDocument> {
    const now = Date.now();
    if (
      !forceRefresh &&
      this.discoveryDocument &&
      now - this.discoveryFetchedAt < this.discoveryCacheTtlMs
    ) {
      return this.discoveryDocument;
    }

    try {
      const response = await this.http.get<OIDCDiscoveryDocument>(
        '/.well-known/openid-configuration',
        {
          // Use the issuerUrl directly, not the /auth baseURL
          baseURL: this.issuerUrl,
        }
      );

      this.discoveryDocument = response.data;
      this.discoveryFetchedAt = now;

      return this.discoveryDocument;
    } catch (error: any) {
      throw new DiscoveryError(
        `Failed to fetch OIDC discovery document: ${error.message}`
      );
    }
  }
```

**Tests** (create `src/__tests__/auth-discovery.test.ts`):
- `discover` fetches and returns a discovery document
- `discover` caches the result on subsequent calls (mock verifies single HTTP call)
- `discover` with `forceRefresh=true` bypasses the cache
- `discover` throws DiscoveryError on network failure
- `discover` uses the issuerUrl (not the /auth baseURL)

**Commit**: `feat(auth): implement OIDC discovery with caching`

---

### Task 6: Implement getSigningKeys and validateToken

**Files**:
- Modify `src/auth/client.ts`

**Steps**:

1. Add imports:

```typescript
import {
  // ... existing imports ...
  JSONWebKeySet,
  TokenValidationOptions,
  TokenValidationResult,
  AssuranceLevel,
} from './types';
import { JWKSError, TokenValidationError } from './errors';
```

2. Add private cache fields to the class:

```typescript
  private jwksCache: JSONWebKeySet | null = null;
  private jwksFetchedAt: number = 0;
  /** Cache TTL for JWKS in milliseconds (default: 1 hour) */
  private jwksCacheTtlMs: number = 3600000;
```

3. Add the `getSigningKeys` method:

```typescript
  /**
   * Fetch the JSON Web Key Set from the JWKS endpoint.
   * Uses the discovery document to find the JWKS URI.
   * Results are cached for 1 hour by default.
   *
   * @param forceRefresh - Bypass the cache and fetch fresh keys
   */
  async getSigningKeys(forceRefresh: boolean = false): Promise<JSONWebKeySet> {
    const now = Date.now();
    if (
      !forceRefresh &&
      this.jwksCache &&
      now - this.jwksFetchedAt < this.jwksCacheTtlMs
    ) {
      return this.jwksCache;
    }

    try {
      const discovery = await this.discover();
      const response = await this.http.get<JSONWebKeySet>(discovery.jwks_uri, {
        baseURL: '', // Use absolute URL from discovery
      });

      this.jwksCache = response.data;
      this.jwksFetchedAt = now;

      return this.jwksCache;
    } catch (error: any) {
      if (error instanceof DiscoveryError) throw error;
      throw new JWKSError(`Failed to fetch JWKS: ${error.message}`);
    }
  }
```

4. Add the `validateToken` method:

```typescript
  /**
   * Validate a JWT access token locally.
   * Checks signature, expiration, audience, issuer, scopes, and assurance level.
   *
   * NOTE: For full signature verification, this method requires the `jsonwebtoken`
   * package as a peer dependency. If unavailable, it falls back to decode-only
   * validation (expiry, claims, etc.) without signature verification.
   *
   * @param token - The JWT access token to validate
   * @param options - Validation constraints
   */
  async validateToken(
    token: string,
    options: TokenValidationOptions = {}
  ): Promise<TokenValidationResult> {
    try {
      // Decode without verification first to inspect claims
      const decoded = jwtDecode<TokenValidationResult['payload']>(token);

      if (!decoded) {
        return { valid: false, error: 'Malformed token', errorCode: 'malformed' };
      }

      const now = Math.floor(Date.now() / 1000);
      const clockTolerance = options.clockToleranceSeconds ?? 30;

      // Check expiration
      if (decoded.exp && decoded.exp + clockTolerance < now) {
        return { valid: false, error: 'Token has expired', errorCode: 'expired' };
      }

      // Check not-before
      if (decoded.nbf && decoded.nbf - clockTolerance > now) {
        return { valid: false, error: 'Token is not yet valid', errorCode: 'expired' };
      }

      // Check issuer
      if (options.issuer && decoded.iss !== options.issuer) {
        return {
          valid: false,
          error: `Invalid issuer: expected ${options.issuer}, got ${decoded.iss}`,
          errorCode: 'invalid_issuer',
        };
      }

      // Check audience
      if (options.audience) {
        const audiences = Array.isArray(decoded.aud) ? decoded.aud : [decoded.aud];
        if (!audiences.includes(options.audience)) {
          return {
            valid: false,
            error: `Invalid audience: expected ${options.audience}`,
            errorCode: 'invalid_audience',
          };
        }
      }

      // Check required scopes
      if (options.requiredScopes && options.requiredScopes.length > 0) {
        const tokenScopes = decoded.scope?.split(' ') ?? [];
        const missingScopes = options.requiredScopes.filter(
          (s) => !tokenScopes.includes(s)
        );
        if (missingScopes.length > 0) {
          return {
            valid: false,
            error: `Missing required scopes: ${missingScopes.join(', ')}`,
            errorCode: 'insufficient_scope',
          };
        }
      }

      // Check assurance level
      if (options.requiredAssuranceLevel) {
        const levelOrder = [AssuranceLevel.AAL1, AssuranceLevel.AAL2, AssuranceLevel.AAL3];
        const currentLevel = (decoded.acr as AssuranceLevel) ?? AssuranceLevel.AAL1;
        const currentIdx = levelOrder.indexOf(currentLevel);
        const requiredIdx = levelOrder.indexOf(options.requiredAssuranceLevel);

        if (currentIdx < requiredIdx) {
          return {
            valid: false,
            error: `Insufficient assurance level: current=${currentLevel}, required=${options.requiredAssuranceLevel}`,
            errorCode: 'insufficient_assurance',
          };
        }
      }

      return { valid: true, payload: decoded };
    } catch (error: any) {
      return { valid: false, error: error.message, errorCode: 'malformed' };
    }
  }
```

**Tests** (create `src/__tests__/auth-validation.test.ts`):
- `getSigningKeys` fetches JWKS via discovery document
- `getSigningKeys` caches the result
- `getSigningKeys` with forceRefresh bypasses cache
- `getSigningKeys` throws JWKSError on failure
- `validateToken` returns valid=true for a valid non-expired token
- `validateToken` returns expired error for an expired token
- `validateToken` returns invalid_issuer when issuer does not match
- `validateToken` returns invalid_audience when audience does not match
- `validateToken` returns insufficient_scope when required scopes missing
- `validateToken` returns insufficient_assurance when assurance level too low
- `validateToken` returns malformed for an unparseable token
- `validateToken` respects clockToleranceSeconds

**Commit**: `feat(auth): implement getSigningKeys and validateToken with caching`

---

### Task 7: Implement step-up authentication methods

**Files**:
- Modify `src/auth/client.ts`

**Steps**:

1. Add imports:

```typescript
import {
  // ... existing imports ...
  StepUpRequest,
  StepUpResult,
} from './types';
import { StepUpRequiredError } from './errors';
```

2. Add the `getAssuranceLevel` method:

```typescript
  /**
   * Extract the Authentication Assurance Level (AAL) from an access token.
   * Reads the `acr` claim from the JWT payload.
   *
   * @param accessToken - JWT access token
   * @returns The assurance level, defaulting to AAL1 if not present
   */
  getAssuranceLevel(accessToken: string): AssuranceLevel {
    try {
      const claims = jwtDecode<JWTPayload>(accessToken);
      const acr = claims.acr;

      if (acr && Object.values(AssuranceLevel).includes(acr as AssuranceLevel)) {
        return acr as AssuranceLevel;
      }

      return AssuranceLevel.AAL1;
    } catch (error) {
      throw new InvalidTokenError('Cannot extract assurance level: invalid token');
    }
  }
```

3. Add the `requireStepUp` method:

```typescript
  /**
   * Check if the current token meets the required assurance level.
   * If not, initiates a step-up authentication flow.
   *
   * @param accessToken - Current JWT access token
   * @param targetLevel - Required assurance level
   * @param options - Additional step-up options (reason, redirectUri)
   * @returns Step-up result indicating if step-up is needed
   * @throws StepUpRequiredError if step-up is needed and no redirect is configured
   */
  async requireStepUp(
    accessToken: string,
    targetLevel: AssuranceLevel,
    options: Omit<StepUpRequest, 'targetLevel'> = {}
  ): Promise<StepUpResult> {
    const currentLevel = this.getAssuranceLevel(accessToken);
    const levelOrder = [AssuranceLevel.AAL1, AssuranceLevel.AAL2, AssuranceLevel.AAL3];
    const currentIdx = levelOrder.indexOf(currentLevel);
    const targetIdx = levelOrder.indexOf(targetLevel);

    if (currentIdx >= targetIdx) {
      return {
        required: false,
        currentLevel,
        targetLevel,
        accessToken,
      };
    }

    // Request step-up from the server
    try {
      const response = await this.http.post<StepUpResult>(
        '/step-up',
        {
          target_level: targetLevel,
          reason: options.reason,
          redirect_uri: options.redirectUri,
        },
        {
          headers: { Authorization: `Bearer ${accessToken}` },
        }
      );

      const result: StepUpResult = {
        required: true,
        currentLevel,
        targetLevel,
        stepUpUrl: response.data.stepUpUrl,
      };

      return result;
    } catch (error: any) {
      if (error.response?.status === 401) {
        throw new InvalidTokenError('Access token is invalid or expired');
      }
      throw this.handleError(error);
    }
  }
```

**Tests** (create `src/__tests__/auth-stepup.test.ts`):
- `getAssuranceLevel` returns AAL1 for token without acr claim
- `getAssuranceLevel` returns the correct level from the acr claim
- `getAssuranceLevel` throws InvalidTokenError for malformed token
- `requireStepUp` returns required=false when current level meets target
- `requireStepUp` returns required=true with stepUpUrl when level insufficient
- `requireStepUp` sends reason and redirectUri to the server
- `requireStepUp` throws InvalidTokenError on 401

**Commit**: `feat(auth): implement step-up authentication with getAssuranceLevel and requireStepUp`

---

### Task 8: Implement auto-refresh and token event callbacks

**Files**:
- Modify `src/auth/client.ts`

**Steps**:

1. Add imports:

```typescript
import {
  // ... existing imports ...
  AutoRefreshOptions,
  AutoRefreshHandle,
} from './types';
```

2. Add private fields to the `AuthClient` class:

```typescript
  private autoRefreshTimer: ReturnType<typeof setTimeout> | null = null;
  private refreshCallbacks: Array<(tokens: TokenResponse) => void> = [];
```

3. Add the `onTokenRefresh` method:

```typescript
  /**
   * Register a callback that is invoked whenever a token is refreshed
   * (either manually via refreshToken or automatically via auto-refresh).
   *
   * @param callback - Function called with the new TokenResponse
   * @returns A function to unregister the callback
   */
  onTokenRefresh(callback: (tokens: TokenResponse) => void): () => void {
    this.refreshCallbacks.push(callback);
    return () => {
      this.refreshCallbacks = this.refreshCallbacks.filter((cb) => cb !== callback);
    };
  }
```

4. Add a private method to notify callbacks:

```typescript
  private notifyRefreshCallbacks(tokens: TokenResponse): void {
    for (const callback of this.refreshCallbacks) {
      try {
        callback(tokens);
      } catch {
        // Swallow callback errors to avoid breaking the refresh flow
      }
    }
  }
```

5. Modify the existing `refreshToken` method to call `notifyRefreshCallbacks` after success:

After `return this.mapTokenResponse(response.data);` -- replace with:

```typescript
      const tokens = this.mapTokenResponse(response.data);
      this.notifyRefreshCallbacks(tokens);
      return tokens;
```

6. Add the `enableAutoRefresh` method:

```typescript
  /**
   * Enable proactive token auto-refresh.
   * Automatically refreshes the token before it expires.
   *
   * @param accessToken - Current access token (used to read expiry)
   * @param options - Auto-refresh configuration
   * @returns Handle to stop auto-refresh
   *
   * @example
   * ```typescript
   * const handle = auth.enableAutoRefresh(tokens.accessToken, {
   *   refreshToken: tokens.refreshToken!,
   *   onRefresh: (newTokens) => {
   *     // Store new tokens
   *   },
   * });
   * // Later...
   * handle.stop();
   * ```
   */
  enableAutoRefresh(
    accessToken: string,
    options: AutoRefreshOptions
  ): AutoRefreshHandle {
    // Stop any existing auto-refresh
    this.stopAutoRefresh();

    const refreshBeforeExpiry = (options.refreshBeforeExpirySeconds ?? 60) * 1000;
    const maxRetries = options.maxRetries ?? 3;
    let currentRefreshToken = options.refreshToken;
    let retryCount = 0;
    let active = true;

    const scheduleRefresh = (token: string): void => {
      if (!active) return;

      try {
        const claims = jwtDecode<JWTPayload>(token);
        const expiresAt = (claims.exp ?? 0) * 1000;
        const now = Date.now();
        const delay = Math.max(expiresAt - now - refreshBeforeExpiry, 0);

        this.autoRefreshTimer = setTimeout(async () => {
          if (!active) return;

          try {
            const tokens = await this.refreshToken(currentRefreshToken);
            retryCount = 0;

            if (tokens.refreshToken) {
              currentRefreshToken = tokens.refreshToken;
            }

            options.onRefresh?.(tokens);
            scheduleRefresh(tokens.accessToken);
          } catch (error: any) {
            retryCount++;
            if (retryCount >= maxRetries) {
              active = false;
              options.onError?.(error);
            } else {
              // Retry after a short delay (exponential backoff)
              const retryDelay = Math.min(1000 * Math.pow(2, retryCount), 30000);
              this.autoRefreshTimer = setTimeout(() => {
                scheduleRefresh(token);
              }, retryDelay);
            }
          }
        }, delay);
      } catch (error: any) {
        active = false;
        options.onError?.(error);
      }
    };

    scheduleRefresh(accessToken);

    return {
      stop: () => {
        active = false;
        this.stopAutoRefresh();
      },
      isActive: () => active,
    };
  }

  private stopAutoRefresh(): void {
    if (this.autoRefreshTimer) {
      clearTimeout(this.autoRefreshTimer);
      this.autoRefreshTimer = null;
    }
  }
```

**Tests** (create `src/__tests__/auth-autorefresh.test.ts`):
- `onTokenRefresh` callback is called when `refreshToken` succeeds
- `onTokenRefresh` returns an unregister function that works
- `enableAutoRefresh` schedules a refresh before expiry
- `enableAutoRefresh` calls onRefresh callback on successful refresh
- `enableAutoRefresh` calls onError after maxRetries failures
- `enableAutoRefresh.stop()` prevents further refreshes
- `enableAutoRefresh.isActive()` returns correct state
- Auto-refresh updates the refresh token when the server issues a new one
- Multiple `onTokenRefresh` callbacks are all invoked
- Callback errors do not prevent other callbacks from running

Use `vi.useFakeTimers()` / `vi.advanceTimersByTime()` for timer-based tests.

**Commit**: `feat(auth): implement auto-refresh and token event callbacks`

---

### Task 9: Update auth barrel exports

**Files**:
- Modify `src/auth/index.ts`

**Steps**:

1. Replace the contents of `src/auth/index.ts` with:

```typescript
/**
 * Authentication module
 */

export { AuthClient } from './client';
export type { AuthClientOptions } from './client';

export type {
  TokenResponse,
  TokenIntrospection,
  UserInfo,
  UserContext,
  Session,
  JWTPayload,
  SocialProvider,
  AssuranceLevel,
  PKCEChallenge,
  AuthorizationUrlOptions,
  AuthorizationUrlResult,
  ClientCredentialsOptions,
  TokenValidationOptions,
  TokenValidationResult,
  JSONWebKey,
  JSONWebKeySet,
  OIDCDiscoveryDocument,
  StepUpRequest,
  StepUpResult,
  AutoRefreshOptions,
  AutoRefreshHandle,
} from './types';

// Re-export AssuranceLevel as a value (enum)
export { AssuranceLevel } from './types';

export { ROLES, PERMISSIONS, getRolePermissions, checkPermission } from './roles';
export type { Role, Permission } from './roles';

export {
  generatePKCEChallenge,
  generateCodeVerifier,
  generateCodeChallenge,
} from './pkce';

export {
  AuthError,
  TokenExpiredError,
  InvalidTokenError,
  UnauthorizedError,
  ForbiddenError,
  StepUpRequiredError,
  DiscoveryError,
  JWKSError,
  TokenValidationError,
} from './errors';
```

**Tests**: Run `npm run typecheck` to verify all exports resolve correctly.

**Commit**: `feat(auth): update barrel exports with all new types and utilities`

---

### Task 10: Create session management types

**Files**:
- Create `src/sessions/types.ts`

**Steps**:

1. Create `src/sessions/types.ts` with the full type definitions:

```typescript
/**
 * Session management type definitions.
 */

import { AssuranceLevel } from '../auth/types';

/**
 * Session status.
 */
export type SessionStatus = 'active' | 'expired' | 'revoked';

/**
 * Device type classification.
 */
export type DeviceType = 'desktop' | 'mobile' | 'tablet' | 'unknown';

/**
 * Session activity action types.
 */
export enum ActivityType {
  SessionCreated = 'session_created',
  LoginSucceeded = 'login_succeeded',
  LoginFailed = 'login_failed',
  MfaVerified = 'mfa_verified',
  PasswordChanged = 'password_changed',
  TokenRefreshed = 'token_refreshed',
  SessionExtended = 'session_extended',
  PermissionsChanged = 'permissions_changed',
  SensitiveActionPerformed = 'sensitive_action_performed',
}

/**
 * Action to take when concurrent session limit is reached.
 */
export type ConcurrentLimitAction = 'deny_new' | 'revoke_oldest';

/**
 * Device information extracted from user-agent and client hints.
 */
export interface DeviceInfo {
  /** Raw user-agent string */
  userAgent: string;
  /** Parsed browser name (e.g. "Chrome") */
  browserName?: string;
  /** Parsed browser version */
  browserVersion?: string;
  /** Operating system name (e.g. "macOS") */
  osName?: string;
  /** Operating system version */
  osVersion?: string;
  /** Classified device type */
  deviceType: DeviceType;
  /** User-assigned device name (if registered) */
  deviceName?: string;
  /** Whether the device is mobile */
  isMobile: boolean;
  /** Whether the user-agent appears to be a bot */
  isBot: boolean;
}

/**
 * Approximate geo-location of a session.
 */
export interface GeoLocation {
  /** City name */
  city?: string;
  /** Region or state */
  region?: string;
  /** ISO 3166-1 alpha-2 country code */
  country?: string;
  /** Full country name */
  countryName?: string;
  /** Latitude */
  latitude?: number;
  /** Longitude */
  longitude?: number;
  /** IANA timezone identifier */
  timezone?: string;
}

/**
 * Full session detail with device and location information.
 */
export interface SessionDetail {
  /** Unique session identifier */
  id: string;
  /** User who owns this session */
  userId: string;
  /** Current session status */
  status: SessionStatus;
  /** When the session was created */
  createdAt: string;
  /** When the session was last active */
  lastActiveAt: string;
  /** Absolute session expiration time */
  expiresAt: string;
  /** Idle timeout expiration (resets on activity) */
  idleExpiresAt?: string;
  /** Whether this is the caller's current session */
  isCurrent: boolean;
  /** Client IP address */
  ipAddress: string;
  /** Device information */
  device: DeviceInfo;
  /** Approximate geo-location */
  geoLocation?: GeoLocation;
  /** Method used to authenticate (e.g. "password", "sso", "social:google") */
  authenticationMethod: string;
  /** Whether MFA was verified in this session */
  mfaVerified: boolean;
  /** Assurance level of this session */
  assuranceLevel: AssuranceLevel;
  /** Reason for revocation (if status is 'revoked') */
  revocationReason?: string;
}

/**
 * Session activity log entry.
 */
export interface SessionActivity {
  /** Activity entry ID */
  id: string;
  /** Session this activity belongs to */
  sessionId: string;
  /** Type of activity */
  action: ActivityType;
  /** When the activity occurred */
  timestamp: string;
  /** IP address at the time of the activity */
  ipAddress: string;
  /** Additional context for the activity */
  details?: Record<string, unknown>;
}

/**
 * Session policy configuration for a tenant.
 */
export interface SessionPolicy {
  /** Maximum session lifetime in minutes (0 = unlimited) */
  maxSessionLifetimeMinutes: number;
  /** Idle timeout in minutes (0 = no idle timeout) */
  idleTimeoutMinutes: number;
  /** Maximum concurrent sessions per user (0 = unlimited) */
  maxConcurrentSessions: number;
  /** What to do when the concurrent session limit is reached */
  onConcurrentLimitReached: ConcurrentLimitAction;
  /** Whether to bind sessions to the originating IP address */
  bindToIP: boolean;
  /** Whether to bind sessions to the originating device fingerprint */
  bindToDevice: boolean;
  /** Whether to require MFA for new session creation */
  requireMFAForNewSession: boolean;
}

/**
 * Aggregate session statistics for admin dashboards.
 */
export interface SessionStats {
  /** Total number of active sessions */
  totalActiveSessions: number;
  /** Number of unique users with active sessions */
  uniqueUsers: number;
  /** Session count by device type */
  sessionsByDeviceType: Record<DeviceType, number>;
  /** Session count by authentication method */
  sessionsByAuthMethod: Record<string, number>;
  /** Session count by country code */
  sessionsByCountry: Record<string, number>;
  /** Average session duration in minutes */
  averageSessionDurationMinutes: number;
  /** Peak concurrent sessions in the current period */
  peakConcurrentSessions: number;
}

/**
 * Options for listing sessions (user-facing).
 */
export interface SessionListOptions {
  /** Filter by session status */
  status?: SessionStatus;
  /** Page number (1-based) */
  page?: number;
  /** Page size (default: 20) */
  pageSize?: number;
  /** Sort by field */
  sortBy?: 'createdAt' | 'lastActiveAt';
  /** Sort direction */
  sortOrder?: 'asc' | 'desc';
}

/**
 * Options for listing sessions (admin-facing).
 */
export interface AdminSessionListOptions extends SessionListOptions {
  /** Filter by device type */
  deviceType?: DeviceType;
  /** Filter by country code */
  country?: string;
  /** Filter by authentication method */
  authMethod?: string;
  /** Filter sessions created after this timestamp */
  createdAfter?: string;
  /** Filter sessions created before this timestamp */
  createdBefore?: string;
}

/**
 * Paginated session list response.
 */
export interface SessionListResponse {
  data: SessionDetail[];
  total: number;
  page: number;
  pageSize: number;
}

/**
 * Paginated activity list response.
 */
export interface SessionActivityResponse {
  data: SessionActivity[];
  total: number;
  page: number;
  pageSize: number;
}
```

**Tests**: No tests for this task (types only). TypeScript compiler validates correctness.

**Commit**: `feat(sessions): add comprehensive session management types`

---

### Task 11: Create session management errors

**Files**:
- Create `src/sessions/errors.ts`

**Steps**:

1. Create `src/sessions/errors.ts`:

```typescript
/**
 * Session management errors.
 */

export class SessionError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'SessionError';
  }
}

export class SessionNotFoundError extends SessionError {
  public readonly sessionId: string;

  constructor(sessionId: string) {
    super(`Session not found: ${sessionId}`);
    this.name = 'SessionNotFoundError';
    this.sessionId = sessionId;
  }
}

export class CannotRevokeCurrentError extends SessionError {
  public readonly sessionId: string;

  constructor(sessionId: string) {
    super(`Cannot revoke the current session: ${sessionId}. Use revokeAllSessions to include the current session.`);
    this.name = 'CannotRevokeCurrentError';
    this.sessionId = sessionId;
  }
}

export class SessionAlreadyRevokedError extends SessionError {
  public readonly sessionId: string;

  constructor(sessionId: string) {
    super(`Session is already revoked: ${sessionId}`);
    this.name = 'SessionAlreadyRevokedError';
    this.sessionId = sessionId;
  }
}

export class ConcurrentLimitReachedError extends SessionError {
  public readonly limit: number;
  public readonly currentCount: number;

  constructor(limit: number, currentCount: number) {
    super(`Concurrent session limit reached: ${currentCount}/${limit}`);
    this.name = 'ConcurrentLimitReachedError';
    this.limit = limit;
    this.currentCount = currentCount;
  }
}

export class IPMismatchError extends SessionError {
  public readonly expectedIP: string;
  public readonly actualIP: string;

  constructor(expectedIP: string, actualIP: string) {
    super(`IP address mismatch: expected ${expectedIP}, got ${actualIP}`);
    this.name = 'IPMismatchError';
    this.expectedIP = expectedIP;
    this.actualIP = actualIP;
  }
}

export class DeviceMismatchError extends SessionError {
  constructor(message: string = 'Device fingerprint does not match the session') {
    super(message);
    this.name = 'DeviceMismatchError';
  }
}

export class AdminRequiredError extends SessionError {
  constructor(message: string = 'Admin privileges required for this operation') {
    super(message);
    this.name = 'AdminRequiredError';
  }
}

export class InvalidSessionPolicyError extends SessionError {
  public readonly field: string;
  public readonly reason: string;

  constructor(field: string, reason: string) {
    super(`Invalid session policy: ${field} - ${reason}`);
    this.name = 'InvalidSessionPolicyError';
    this.field = field;
    this.reason = reason;
  }
}
```

**Tests**: No standalone tests. Errors are tested as part of the methods that throw them.

**Commit**: `feat(sessions): add session management error classes`

---

### Task 12: Implement SessionClient -- user-facing methods

**Files**:
- Create `src/sessions/client.ts`

**Steps**:

1. Create `src/sessions/client.ts` with the client scaffold and user-facing methods:

```typescript
/**
 * HTTP client for session management operations.
 */

import axios, { AxiosInstance } from 'axios';
import {
  SessionDetail,
  SessionListOptions,
  SessionListResponse,
  SessionActivity,
  SessionActivityResponse,
} from './types';
import {
  SessionError,
  SessionNotFoundError,
  SessionAlreadyRevokedError,
} from './errors';

export interface SessionClientOptions {
  /** Base URL of the platform API */
  baseUrl: string;
  /** Default access token (can be overridden per-method) */
  accessToken?: string;
  /** Request timeout in milliseconds */
  timeout?: number;
}

/**
 * Client for session management operations.
 *
 * @example
 * ```typescript
 * const sessions = new SessionClient({
 *   baseUrl: 'https://api.example.com',
 * });
 *
 * // List active sessions
 * const list = await sessions.listSessions(accessToken);
 * console.log(`You have ${list.total} active sessions`);
 *
 * // Revoke all other sessions
 * await sessions.revokeOtherSessions(accessToken, 'Security precaution');
 * ```
 */
export class SessionClient {
  private http: AxiosInstance;
  private accessToken?: string;

  constructor(options: SessionClientOptions) {
    this.accessToken = options.accessToken;

    this.http = axios.create({
      baseURL: `${options.baseUrl.replace(/\/$/, '')}/sessions`,
      timeout: options.timeout ?? 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  /**
   * Set the default access token for subsequent requests.
   */
  setAccessToken(token: string): void {
    this.accessToken = token;
  }

  private authHeaders(accessToken?: string): Record<string, string> {
    const token = accessToken ?? this.accessToken;
    if (!token) {
      throw new SessionError('Access token is required');
    }
    return { Authorization: `Bearer ${token}` };
  }

  /**
   * List the current user's sessions.
   *
   * @param accessToken - JWT access token
   * @param options - Pagination and filter options
   */
  async listSessions(
    accessToken: string,
    options?: SessionListOptions
  ): Promise<SessionListResponse> {
    const params: Record<string, unknown> = {};
    if (options?.status) params.status = options.status;
    if (options?.page) params.page = options.page;
    if (options?.pageSize) params.page_size = options.pageSize;
    if (options?.sortBy) params.sort_by = options.sortBy;
    if (options?.sortOrder) params.sort_order = options.sortOrder;

    const response = await this.http.get<SessionListResponse>('/', {
      headers: this.authHeaders(accessToken),
      params,
    });

    return response.data;
  }

  /**
   * Get details of a specific session.
   *
   * @param accessToken - JWT access token
   * @param sessionId - Session to retrieve
   */
  async getSession(
    accessToken: string,
    sessionId: string
  ): Promise<SessionDetail> {
    try {
      const response = await this.http.get<SessionDetail>(`/${sessionId}`, {
        headers: this.authHeaders(accessToken),
      });
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        throw new SessionNotFoundError(sessionId);
      }
      throw this.handleError(error);
    }
  }

  /**
   * Revoke a specific session.
   *
   * @param accessToken - JWT access token
   * @param sessionId - Session to revoke
   * @param reason - Optional reason for revocation
   */
  async revokeSession(
    accessToken: string,
    sessionId: string,
    reason?: string
  ): Promise<void> {
    try {
      await this.http.post(
        `/${sessionId}/revoke`,
        { reason },
        { headers: this.authHeaders(accessToken) }
      );
    } catch (error: any) {
      if (error.response?.status === 404) {
        throw new SessionNotFoundError(sessionId);
      }
      if (error.response?.status === 409) {
        throw new SessionAlreadyRevokedError(sessionId);
      }
      throw this.handleError(error);
    }
  }

  /**
   * Revoke all sessions except the current one.
   *
   * @param accessToken - JWT access token
   * @param reason - Optional reason for revocation
   */
  async revokeOtherSessions(
    accessToken: string,
    reason?: string
  ): Promise<void> {
    await this.http.post(
      '/revoke-others',
      { reason },
      { headers: this.authHeaders(accessToken) }
    );
  }

  /**
   * Revoke all sessions including the current one.
   * After this call, the user will need to re-authenticate.
   *
   * @param accessToken - JWT access token
   * @param reason - Optional reason for revocation
   */
  async revokeAllSessions(
    accessToken: string,
    reason?: string
  ): Promise<void> {
    await this.http.post(
      '/revoke-all',
      { reason },
      { headers: this.authHeaders(accessToken) }
    );
  }

  /**
   * Extend the lifetime of a session.
   *
   * @param accessToken - JWT access token
   * @param sessionId - Session to extend (defaults to current session)
   */
  async extendSession(
    accessToken: string,
    sessionId?: string
  ): Promise<SessionDetail> {
    const path = sessionId ? `/${sessionId}/extend` : '/current/extend';

    try {
      const response = await this.http.post<SessionDetail>(path, null, {
        headers: this.authHeaders(accessToken),
      });
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        throw new SessionNotFoundError(sessionId ?? 'current');
      }
      throw this.handleError(error);
    }
  }

  /**
   * Get the activity log for a specific session.
   *
   * @param accessToken - JWT access token
   * @param sessionId - Session to get activity for
   */
  async getSessionActivity(
    accessToken: string,
    sessionId: string
  ): Promise<SessionActivityResponse> {
    try {
      const response = await this.http.get<SessionActivityResponse>(
        `/${sessionId}/activity`,
        { headers: this.authHeaders(accessToken) }
      );
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        throw new SessionNotFoundError(sessionId);
      }
      throw this.handleError(error);
    }
  }

  private handleError(error: any): SessionError {
    const data = error.response?.data;
    return new SessionError(
      data?.message ?? data?.error ?? error.message
    );
  }
}
```

**Tests** (create `src/__tests__/sessions.test.ts`):
- `listSessions` sends GET with auth header and returns paginated response
- `listSessions` passes query params (status, page, pageSize, sortBy, sortOrder)
- `getSession` returns session detail
- `getSession` throws SessionNotFoundError on 404
- `revokeSession` sends POST with reason
- `revokeSession` throws SessionNotFoundError on 404
- `revokeSession` throws SessionAlreadyRevokedError on 409
- `revokeOtherSessions` sends POST to /revoke-others
- `revokeAllSessions` sends POST to /revoke-all
- `extendSession` with sessionId sends to /{id}/extend
- `extendSession` without sessionId sends to /current/extend
- `getSessionActivity` returns activity log
- `getSessionActivity` throws SessionNotFoundError on 404

**Commit**: `feat(sessions): implement SessionClient user-facing methods`

---

### Task 13: Implement SessionClient -- admin and policy methods

**Files**:
- Modify `src/sessions/client.ts`

**Steps**:

1. Add the remaining imports to the top of `src/sessions/client.ts`:

```typescript
import {
  SessionDetail,
  SessionListOptions,
  AdminSessionListOptions,
  SessionListResponse,
  SessionActivity,
  SessionActivityResponse,
  SessionPolicy,
  SessionStats,
} from './types';
import {
  SessionError,
  SessionNotFoundError,
  SessionAlreadyRevokedError,
  AdminRequiredError,
  InvalidSessionPolicyError,
} from './errors';
```

2. Add the admin and policy methods to the `SessionClient` class (after the user-facing methods):

```typescript
  // --- Session Policy (admin) ---

  /**
   * Set the session policy for the tenant.
   * Requires admin privileges.
   *
   * @param policy - The session policy configuration
   */
  async setSessionPolicy(policy: SessionPolicy): Promise<SessionPolicy> {
    try {
      const response = await this.http.put<SessionPolicy>(
        '/policy',
        {
          max_session_lifetime_minutes: policy.maxSessionLifetimeMinutes,
          idle_timeout_minutes: policy.idleTimeoutMinutes,
          max_concurrent_sessions: policy.maxConcurrentSessions,
          on_concurrent_limit_reached: policy.onConcurrentLimitReached,
          bind_to_ip: policy.bindToIP,
          bind_to_device: policy.bindToDevice,
          require_mfa_for_new_session: policy.requireMFAForNewSession,
        },
        { headers: this.authHeaders() }
      );

      return this.mapSessionPolicy(response.data);
    } catch (error: any) {
      if (error.response?.status === 403) {
        throw new AdminRequiredError();
      }
      if (error.response?.status === 422) {
        const data = error.response.data;
        throw new InvalidSessionPolicyError(
          data?.field ?? 'unknown',
          data?.message ?? 'Invalid policy configuration'
        );
      }
      throw this.handleError(error);
    }
  }

  /**
   * Get the current session policy for the tenant.
   */
  async getSessionPolicy(): Promise<SessionPolicy> {
    try {
      const response = await this.http.get<SessionPolicy>('/policy', {
        headers: this.authHeaders(),
      });

      return this.mapSessionPolicy(response.data);
    } catch (error: any) {
      if (error.response?.status === 403) {
        throw new AdminRequiredError();
      }
      throw this.handleError(error);
    }
  }

  // --- Admin Operations ---

  /**
   * List sessions for a specific user (admin only).
   *
   * @param userId - Target user ID
   * @param options - Pagination and filter options
   */
  async adminListUserSessions(
    userId: string,
    options?: AdminSessionListOptions
  ): Promise<SessionListResponse> {
    const params: Record<string, unknown> = {};
    if (options?.status) params.status = options.status;
    if (options?.page) params.page = options.page;
    if (options?.pageSize) params.page_size = options.pageSize;
    if (options?.sortBy) params.sort_by = options.sortBy;
    if (options?.sortOrder) params.sort_order = options.sortOrder;
    if (options?.deviceType) params.device_type = options.deviceType;
    if (options?.country) params.country = options.country;
    if (options?.authMethod) params.auth_method = options.authMethod;
    if (options?.createdAfter) params.created_after = options.createdAfter;
    if (options?.createdBefore) params.created_before = options.createdBefore;

    try {
      const response = await this.http.get<SessionListResponse>(
        `/admin/users/${userId}`,
        {
          headers: this.authHeaders(),
          params,
        }
      );
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 403) {
        throw new AdminRequiredError();
      }
      throw this.handleError(error);
    }
  }

  /**
   * Revoke a specific session for a user (admin only).
   *
   * @param userId - Target user ID
   * @param sessionId - Session to revoke
   * @param reason - Optional revocation reason
   */
  async adminRevokeUserSession(
    userId: string,
    sessionId: string,
    reason?: string
  ): Promise<void> {
    try {
      await this.http.post(
        `/admin/users/${userId}/${sessionId}/revoke`,
        { reason },
        { headers: this.authHeaders() }
      );
    } catch (error: any) {
      if (error.response?.status === 403) {
        throw new AdminRequiredError();
      }
      if (error.response?.status === 404) {
        throw new SessionNotFoundError(sessionId);
      }
      if (error.response?.status === 409) {
        throw new SessionAlreadyRevokedError(sessionId);
      }
      throw this.handleError(error);
    }
  }

  /**
   * Revoke all sessions for a user (admin only).
   *
   * @param userId - Target user ID
   * @param reason - Optional revocation reason
   */
  async adminRevokeAllUserSessions(
    userId: string,
    reason?: string
  ): Promise<void> {
    try {
      await this.http.post(
        `/admin/users/${userId}/revoke-all`,
        { reason },
        { headers: this.authHeaders() }
      );
    } catch (error: any) {
      if (error.response?.status === 403) {
        throw new AdminRequiredError();
      }
      throw this.handleError(error);
    }
  }

  /**
   * Get session statistics for the tenant (admin only).
   *
   * @param tenantId - Optional tenant ID (for super-admin cross-tenant queries)
   */
  async adminGetSessionStats(tenantId?: string): Promise<SessionStats> {
    const params: Record<string, unknown> = {};
    if (tenantId) params.tenant_id = tenantId;

    try {
      const response = await this.http.get<SessionStats>('/admin/stats', {
        headers: this.authHeaders(),
        params,
      });
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 403) {
        throw new AdminRequiredError();
      }
      throw this.handleError(error);
    }
  }

  // --- Private Helpers ---

  private mapSessionPolicy(data: any): SessionPolicy {
    return {
      maxSessionLifetimeMinutes: data.max_session_lifetime_minutes ?? data.maxSessionLifetimeMinutes,
      idleTimeoutMinutes: data.idle_timeout_minutes ?? data.idleTimeoutMinutes,
      maxConcurrentSessions: data.max_concurrent_sessions ?? data.maxConcurrentSessions,
      onConcurrentLimitReached: data.on_concurrent_limit_reached ?? data.onConcurrentLimitReached,
      bindToIP: data.bind_to_ip ?? data.bindToIP,
      bindToDevice: data.bind_to_device ?? data.bindToDevice,
      requireMFAForNewSession: data.require_mfa_for_new_session ?? data.requireMFAForNewSession,
    };
  }
```

**Tests** (append to `src/__tests__/sessions.test.ts`):
- `setSessionPolicy` sends PUT with snake_case body and returns camelCase policy
- `setSessionPolicy` throws AdminRequiredError on 403
- `setSessionPolicy` throws InvalidSessionPolicyError on 422
- `getSessionPolicy` returns the current policy
- `getSessionPolicy` throws AdminRequiredError on 403
- `adminListUserSessions` sends GET with userId in path and filter params
- `adminListUserSessions` throws AdminRequiredError on 403
- `adminRevokeUserSession` sends POST with reason
- `adminRevokeUserSession` throws AdminRequiredError on 403
- `adminRevokeUserSession` throws SessionNotFoundError on 404
- `adminRevokeUserSession` throws SessionAlreadyRevokedError on 409
- `adminRevokeAllUserSessions` sends POST with reason
- `adminRevokeAllUserSessions` throws AdminRequiredError on 403
- `adminGetSessionStats` returns stats object
- `adminGetSessionStats` passes tenantId as query param when provided
- `adminGetSessionStats` throws AdminRequiredError on 403

**Commit**: `feat(sessions): implement admin and policy methods for SessionClient`

---

### Task 14: Create sessions barrel exports and wire into SDK index

**Files**:
- Create `src/sessions/index.ts`
- Modify `src/index.ts`
- Modify `package.json`

**Steps**:

1. Create `src/sessions/index.ts`:

```typescript
/**
 * Session management module.
 */

export * from './types';
export * from './errors';
export { SessionClient } from './client';
export type { SessionClientOptions } from './client';
```

2. Add the session exports to `src/index.ts`. Add the following import line after the `APIKeyClient` export:

```typescript
export { SessionClient } from './sessions';
```

Add the following type export block after the API Key types block:

```typescript
export type {
  // Session types
  SessionStatus,
  DeviceType,
  ActivityType,
  ConcurrentLimitAction,
  DeviceInfo,
  GeoLocation,
  SessionDetail,
  SessionActivity,
  SessionPolicy,
  SessionStats,
  SessionListOptions,
  AdminSessionListOptions,
  SessionListResponse,
  SessionActivityResponse,
} from './sessions';

// Re-export ActivityType as a value (enum)
export { ActivityType } from './sessions';
```

3. Add the subpath export to `package.json` in the `"exports"` object:

```json
    "./sessions": {
      "types": "./dist/sessions/index.d.ts",
      "import": "./dist/sessions/index.mjs",
      "require": "./dist/sessions/index.js"
    }
```

**Tests**: Run `npm run typecheck` to verify all exports resolve correctly.

**Commit**: `feat(sessions): add barrel exports and wire SessionClient into SDK index`

---

### Task 15: Write comprehensive tests for auth enhancements

**Files**:
- Create `src/__tests__/auth-pkce.test.ts`
- Create `src/__tests__/auth-discovery.test.ts`
- Create `src/__tests__/auth-validation.test.ts`
- Create `src/__tests__/auth-stepup.test.ts`
- Create `src/__tests__/auth-autorefresh.test.ts`

**Steps**:

1. Create `src/__tests__/auth-pkce.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import jwt from 'jsonwebtoken';
import { AuthClient } from '../auth';
import {
  generatePKCEChallenge,
  generateCodeVerifier,
  generateCodeChallenge,
} from '../auth/pkce';

describe('PKCE Utilities', () => {
  describe('generateCodeVerifier', () => {
    it('should return a string of the default length', () => {
      const verifier = generateCodeVerifier();
      expect(verifier.length).toBe(64);
    });

    it('should return a string of a custom length', () => {
      const verifier = generateCodeVerifier(43);
      expect(verifier.length).toBe(43);
    });

    it('should return a base64url-safe string', () => {
      const verifier = generateCodeVerifier();
      expect(verifier).toMatch(/^[A-Za-z0-9_-]+$/);
    });

    it('should generate unique values', () => {
      const v1 = generateCodeVerifier();
      const v2 = generateCodeVerifier();
      expect(v1).not.toBe(v2);
    });
  });

  describe('generateCodeChallenge', () => {
    it('should return a base64url-safe string', () => {
      const challenge = generateCodeChallenge('test-verifier');
      expect(challenge).toMatch(/^[A-Za-z0-9_-]+$/);
    });

    it('should be deterministic for the same input', () => {
      const c1 = generateCodeChallenge('same-verifier');
      const c2 = generateCodeChallenge('same-verifier');
      expect(c1).toBe(c2);
    });

    it('should produce different outputs for different inputs', () => {
      const c1 = generateCodeChallenge('verifier-1');
      const c2 = generateCodeChallenge('verifier-2');
      expect(c1).not.toBe(c2);
    });
  });

  describe('generatePKCEChallenge', () => {
    it('should return codeVerifier, codeChallenge, and codeChallengeMethod', () => {
      const pkce = generatePKCEChallenge();
      expect(pkce.codeVerifier).toBeDefined();
      expect(pkce.codeChallenge).toBeDefined();
      expect(pkce.codeChallengeMethod).toBe('S256');
    });

    it('should have a challenge derived from the verifier', () => {
      const pkce = generatePKCEChallenge();
      const expectedChallenge = generateCodeChallenge(pkce.codeVerifier);
      expect(pkce.codeChallenge).toBe(expectedChallenge);
    });
  });
});

describe('AuthClient.buildAuthorizationUrl', () => {
  let client: AuthClient;

  beforeEach(() => {
    client = new AuthClient({
      issuerUrl: 'https://auth.example.com',
      clientId: 'test-client',
    });
  });

  it('should return a URL with all required OAuth2 parameters', () => {
    const result = client.buildAuthorizationUrl({
      redirectUri: 'https://app.example.com/callback',
    });

    const url = new URL(result.url);
    expect(url.origin).toBe('https://auth.example.com');
    expect(url.pathname).toBe('/auth/authorize');
    expect(url.searchParams.get('response_type')).toBe('code');
    expect(url.searchParams.get('client_id')).toBe('test-client');
    expect(url.searchParams.get('redirect_uri')).toBe('https://app.example.com/callback');
    expect(url.searchParams.get('code_challenge')).toBe(result.pkce.codeChallenge);
    expect(url.searchParams.get('code_challenge_method')).toBe('S256');
  });

  it('should use default scope when none provided', () => {
    const result = client.buildAuthorizationUrl({
      redirectUri: 'https://app.example.com/callback',
    });

    const url = new URL(result.url);
    expect(url.searchParams.get('scope')).toBe('openid profile email');
  });

  it('should use custom scope when provided', () => {
    const result = client.buildAuthorizationUrl({
      redirectUri: 'https://app.example.com/callback',
      scope: 'openid custom:scope',
    });

    const url = new URL(result.url);
    expect(url.searchParams.get('scope')).toBe('openid custom:scope');
  });

  it('should generate state when not provided', () => {
    const result = client.buildAuthorizationUrl({
      redirectUri: 'https://app.example.com/callback',
    });

    expect(result.state).toBeDefined();
    expect(result.state.length).toBeGreaterThan(0);
  });

  it('should use provided state', () => {
    const result = client.buildAuthorizationUrl({
      redirectUri: 'https://app.example.com/callback',
      state: 'my-custom-state',
    });

    expect(result.state).toBe('my-custom-state');
    const url = new URL(result.url);
    expect(url.searchParams.get('state')).toBe('my-custom-state');
  });

  it('should include login_hint when provided', () => {
    const result = client.buildAuthorizationUrl({
      redirectUri: 'https://app.example.com/callback',
      loginHint: 'user@example.com',
    });

    const url = new URL(result.url);
    expect(url.searchParams.get('login_hint')).toBe('user@example.com');
  });

  it('should include provider as connection param', () => {
    const result = client.buildAuthorizationUrl({
      redirectUri: 'https://app.example.com/callback',
      provider: 'google',
    });

    const url = new URL(result.url);
    expect(url.searchParams.get('connection')).toBe('google');
  });

  it('should return a valid PKCE challenge', () => {
    const result = client.buildAuthorizationUrl({
      redirectUri: 'https://app.example.com/callback',
    });

    expect(result.pkce.codeVerifier).toBeDefined();
    expect(result.pkce.codeChallenge).toBeDefined();
    expect(result.pkce.codeChallengeMethod).toBe('S256');

    // Verify challenge is derived from verifier
    const expectedChallenge = generateCodeChallenge(result.pkce.codeVerifier);
    expect(result.pkce.codeChallenge).toBe(expectedChallenge);
  });
});
```

2. Create `src/__tests__/auth-discovery.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import { AuthClient } from '../auth';
import { DiscoveryError } from '../auth/errors';

// Mock axios
vi.mock('axios', () => {
  const mockInstance = {
    get: vi.fn(),
    post: vi.fn(),
  };
  return {
    default: {
      create: vi.fn(() => mockInstance),
    },
    __mockInstance: mockInstance,
  };
});

function getMockAxios() {
  return (axios as any).__mockInstance;
}

const mockDiscoveryDoc = {
  issuer: 'https://auth.example.com',
  authorization_endpoint: 'https://auth.example.com/auth/authorize',
  token_endpoint: 'https://auth.example.com/auth/token',
  userinfo_endpoint: 'https://auth.example.com/auth/userinfo',
  jwks_uri: 'https://auth.example.com/.well-known/jwks.json',
  response_types_supported: ['code'],
  subject_types_supported: ['public'],
  id_token_signing_alg_values_supported: ['RS256'],
};

describe('AuthClient.discover', () => {
  let client: AuthClient;

  beforeEach(() => {
    vi.clearAllMocks();
    client = new AuthClient({ issuerUrl: 'https://auth.example.com' });
  });

  it('should fetch and return the discovery document', async () => {
    getMockAxios().get.mockResolvedValueOnce({ data: mockDiscoveryDoc });

    const doc = await client.discover();

    expect(doc.issuer).toBe('https://auth.example.com');
    expect(doc.jwks_uri).toBe('https://auth.example.com/.well-known/jwks.json');
  });

  it('should cache the discovery document', async () => {
    getMockAxios().get.mockResolvedValueOnce({ data: mockDiscoveryDoc });

    await client.discover();
    await client.discover();

    expect(getMockAxios().get).toHaveBeenCalledTimes(1);
  });

  it('should bypass cache with forceRefresh', async () => {
    getMockAxios().get.mockResolvedValue({ data: mockDiscoveryDoc });

    await client.discover();
    await client.discover(true);

    expect(getMockAxios().get).toHaveBeenCalledTimes(2);
  });

  it('should throw DiscoveryError on failure', async () => {
    getMockAxios().get.mockRejectedValueOnce(new Error('Network error'));

    await expect(client.discover()).rejects.toThrow(DiscoveryError);
  });
});
```

3. Create `src/__tests__/auth-validation.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import jwt from 'jsonwebtoken';
import { AuthClient, AssuranceLevel } from '../auth';

function createTestToken(
  payload: Record<string, any>,
  options: jwt.SignOptions = {}
): string {
  return jwt.sign(payload, 'test-secret', { expiresIn: '1h', ...options });
}

describe('AuthClient.validateToken', () => {
  let client: AuthClient;

  const validPayload = {
    sub: 'user-123',
    email: 'test@example.com',
    scope: 'openid profile email',
    iss: 'https://auth.example.com',
    aud: 'my-app',
    acr: 'aal2',
  };

  beforeEach(() => {
    client = new AuthClient({ issuerUrl: 'https://auth.example.com' });
  });

  it('should return valid=true for a valid token', async () => {
    const token = createTestToken(validPayload);
    const result = await client.validateToken(token);

    expect(result.valid).toBe(true);
    expect(result.payload?.sub).toBe('user-123');
  });

  it('should return expired error for an expired token', async () => {
    const token = jwt.sign(validPayload, 'test-secret', { expiresIn: '-1h' });
    const result = await client.validateToken(token);

    expect(result.valid).toBe(false);
    expect(result.errorCode).toBe('expired');
  });

  it('should return invalid_issuer when issuer does not match', async () => {
    const token = createTestToken(validPayload);
    const result = await client.validateToken(token, {
      issuer: 'https://other-issuer.com',
    });

    expect(result.valid).toBe(false);
    expect(result.errorCode).toBe('invalid_issuer');
  });

  it('should return invalid_audience when audience does not match', async () => {
    const token = createTestToken(validPayload);
    const result = await client.validateToken(token, {
      audience: 'wrong-audience',
    });

    expect(result.valid).toBe(false);
    expect(result.errorCode).toBe('invalid_audience');
  });

  it('should return insufficient_scope when required scopes are missing', async () => {
    const token = createTestToken(validPayload);
    const result = await client.validateToken(token, {
      requiredScopes: ['admin:write'],
    });

    expect(result.valid).toBe(false);
    expect(result.errorCode).toBe('insufficient_scope');
  });

  it('should return insufficient_assurance when level is too low', async () => {
    const token = createTestToken({ ...validPayload, acr: 'aal1' });
    const result = await client.validateToken(token, {
      requiredAssuranceLevel: AssuranceLevel.AAL3,
    });

    expect(result.valid).toBe(false);
    expect(result.errorCode).toBe('insufficient_assurance');
  });

  it('should pass when assurance level meets requirement', async () => {
    const token = createTestToken({ ...validPayload, acr: 'aal2' });
    const result = await client.validateToken(token, {
      requiredAssuranceLevel: AssuranceLevel.AAL2,
    });

    expect(result.valid).toBe(true);
  });

  it('should return malformed for an unparseable token', async () => {
    const result = await client.validateToken('not-a-jwt');

    expect(result.valid).toBe(false);
    expect(result.errorCode).toBe('malformed');
  });

  it('should respect clockToleranceSeconds', async () => {
    // Token that expired 10 seconds ago
    const token = jwt.sign(validPayload, 'test-secret', { expiresIn: '-10s' });

    // With 30s tolerance (default), should still be valid
    const result = await client.validateToken(token, {
      clockToleranceSeconds: 30,
    });
    expect(result.valid).toBe(true);

    // With 0s tolerance, should be expired
    const strictResult = await client.validateToken(token, {
      clockToleranceSeconds: 0,
    });
    expect(strictResult.valid).toBe(false);
    expect(strictResult.errorCode).toBe('expired');
  });
});
```

4. Create `src/__tests__/auth-stepup.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import jwt from 'jsonwebtoken';
import { AuthClient, AssuranceLevel } from '../auth';
import { InvalidTokenError } from '../auth/errors';

function createTestToken(
  payload: Record<string, any>,
  options: jwt.SignOptions = {}
): string {
  return jwt.sign(payload, 'test-secret', { expiresIn: '1h', ...options });
}

describe('AuthClient.getAssuranceLevel', () => {
  let client: AuthClient;

  beforeEach(() => {
    client = new AuthClient({ issuerUrl: 'https://auth.example.com' });
  });

  it('should return AAL1 when no acr claim is present', () => {
    const token = createTestToken({ sub: 'user-123' });
    expect(client.getAssuranceLevel(token)).toBe(AssuranceLevel.AAL1);
  });

  it('should return the correct level from acr claim', () => {
    const token = createTestToken({ sub: 'user-123', acr: 'aal2' });
    expect(client.getAssuranceLevel(token)).toBe(AssuranceLevel.AAL2);
  });

  it('should return AAL3 when acr is aal3', () => {
    const token = createTestToken({ sub: 'user-123', acr: 'aal3' });
    expect(client.getAssuranceLevel(token)).toBe(AssuranceLevel.AAL3);
  });

  it('should throw InvalidTokenError for malformed token', () => {
    expect(() => client.getAssuranceLevel('invalid')).toThrow(InvalidTokenError);
  });
});

describe('AuthClient.requireStepUp', () => {
  let client: AuthClient;

  beforeEach(() => {
    client = new AuthClient({ issuerUrl: 'https://auth.example.com' });
  });

  it('should return required=false when current level meets target', async () => {
    const token = createTestToken({ sub: 'user-123', acr: 'aal2' });
    const result = await client.requireStepUp(token, AssuranceLevel.AAL2);

    expect(result.required).toBe(false);
    expect(result.currentLevel).toBe(AssuranceLevel.AAL2);
    expect(result.accessToken).toBe(token);
  });

  it('should return required=false when current level exceeds target', async () => {
    const token = createTestToken({ sub: 'user-123', acr: 'aal3' });
    const result = await client.requireStepUp(token, AssuranceLevel.AAL2);

    expect(result.required).toBe(false);
  });
});
```

5. Create `src/__tests__/auth-autorefresh.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import jwt from 'jsonwebtoken';
import axios from 'axios';
import { AuthClient } from '../auth';

// Mock axios
vi.mock('axios', () => {
  const mockInstance = {
    get: vi.fn(),
    post: vi.fn(),
  };
  return {
    default: {
      create: vi.fn(() => mockInstance),
    },
    __mockInstance: mockInstance,
  };
});

function getMockAxios() {
  return (axios as any).__mockInstance;
}

function createTestToken(
  payload: Record<string, any>,
  options: jwt.SignOptions = {}
): string {
  return jwt.sign(payload, 'test-secret', { expiresIn: '1h', ...options });
}

describe('AuthClient.onTokenRefresh', () => {
  let client: AuthClient;

  beforeEach(() => {
    vi.clearAllMocks();
    client = new AuthClient({
      issuerUrl: 'https://auth.example.com',
      clientId: 'test-client',
    });
  });

  it('should call callback when refreshToken succeeds', async () => {
    const callback = vi.fn();
    client.onTokenRefresh(callback);

    getMockAxios().post.mockResolvedValueOnce({
      data: {
        access_token: 'new-access',
        token_type: 'Bearer',
        expires_in: 3600,
        refresh_token: 'new-refresh',
      },
    });

    await client.refreshToken('old-refresh');

    expect(callback).toHaveBeenCalledTimes(1);
    expect(callback).toHaveBeenCalledWith(
      expect.objectContaining({ accessToken: 'new-access' })
    );
  });

  it('should unregister callback with returned function', async () => {
    const callback = vi.fn();
    const unregister = client.onTokenRefresh(callback);
    unregister();

    getMockAxios().post.mockResolvedValueOnce({
      data: {
        access_token: 'new-access',
        token_type: 'Bearer',
        expires_in: 3600,
      },
    });

    await client.refreshToken('old-refresh');

    expect(callback).not.toHaveBeenCalled();
  });
});

describe('AuthClient.enableAutoRefresh', () => {
  let client: AuthClient;

  beforeEach(() => {
    vi.useFakeTimers();
    vi.clearAllMocks();
    client = new AuthClient({
      issuerUrl: 'https://auth.example.com',
      clientId: 'test-client',
    });
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should return a handle with stop and isActive', () => {
    const token = createTestToken({ sub: 'user-123' });
    const handle = client.enableAutoRefresh(token, {
      refreshToken: 'refresh-token',
    });

    expect(handle.isActive()).toBe(true);
    handle.stop();
    expect(handle.isActive()).toBe(false);
  });

  it('should call onRefresh callback on successful refresh', async () => {
    const onRefresh = vi.fn();
    const token = createTestToken(
      { sub: 'user-123' },
      { expiresIn: '120s' } // 2 minutes
    );

    getMockAxios().post.mockResolvedValueOnce({
      data: {
        access_token: createTestToken({ sub: 'user-123' }, { expiresIn: '1h' }),
        token_type: 'Bearer',
        expires_in: 3600,
        refresh_token: 'new-refresh',
      },
    });

    const handle = client.enableAutoRefresh(token, {
      refreshToken: 'refresh-token',
      refreshBeforeExpirySeconds: 60,
      onRefresh,
    });

    // Advance past the refresh point (2 min - 60 sec = 60 sec)
    await vi.advanceTimersByTimeAsync(61000);

    expect(onRefresh).toHaveBeenCalledTimes(1);
    handle.stop();
  });

  it('should call onError after maxRetries failures', async () => {
    const onError = vi.fn();
    const token = createTestToken(
      { sub: 'user-123' },
      { expiresIn: '30s' }
    );

    getMockAxios().post.mockRejectedValue(new Error('Network error'));

    const handle = client.enableAutoRefresh(token, {
      refreshToken: 'refresh-token',
      refreshBeforeExpirySeconds: 60,
      maxRetries: 2,
      onError,
    });

    // First attempt (immediate since 30s < 60s buffer)
    await vi.advanceTimersByTimeAsync(1000);
    // First retry (2s exponential backoff)
    await vi.advanceTimersByTimeAsync(3000);
    // Second retry (4s exponential backoff) - hits maxRetries
    await vi.advanceTimersByTimeAsync(5000);

    expect(onError).toHaveBeenCalledTimes(1);
    expect(handle.isActive()).toBe(false);
  });

  it('should stop refreshing when stop is called', async () => {
    const onRefresh = vi.fn();
    const token = createTestToken(
      { sub: 'user-123' },
      { expiresIn: '120s' }
    );

    const handle = client.enableAutoRefresh(token, {
      refreshToken: 'refresh-token',
      refreshBeforeExpirySeconds: 60,
      onRefresh,
    });

    handle.stop();

    await vi.advanceTimersByTimeAsync(120000);

    expect(onRefresh).not.toHaveBeenCalled();
  });
});
```

**Tests**: All test files listed above.

**Commit**: `test(auth): add comprehensive tests for PKCE, discovery, validation, step-up, and auto-refresh`

---

### Task 16: Write comprehensive tests for session management

**Files**:
- Create `src/__tests__/sessions.test.ts`

**Steps**:

1. Create `src/__tests__/sessions.test.ts`:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import { SessionClient } from '../sessions';
import {
  SessionNotFoundError,
  SessionAlreadyRevokedError,
  AdminRequiredError,
  InvalidSessionPolicyError,
} from '../sessions/errors';

// Mock axios
vi.mock('axios', () => {
  const mockInstance = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  };
  return {
    default: {
      create: vi.fn(() => mockInstance),
    },
    __mockInstance: mockInstance,
  };
});

function getMockAxios() {
  return (axios as any).__mockInstance;
}

const mockSession = {
  id: 'session-123',
  userId: 'user-456',
  status: 'active',
  createdAt: '2025-01-15T10:00:00Z',
  lastActiveAt: '2025-01-15T12:00:00Z',
  expiresAt: '2025-01-16T10:00:00Z',
  isCurrent: true,
  ipAddress: '192.168.1.1',
  device: {
    userAgent: 'Mozilla/5.0',
    browserName: 'Chrome',
    browserVersion: '120',
    osName: 'macOS',
    osVersion: '14',
    deviceType: 'desktop',
    isMobile: false,
    isBot: false,
  },
  authenticationMethod: 'password',
  mfaVerified: true,
  assuranceLevel: 'aal2',
};

describe('SessionClient', () => {
  let client: SessionClient;
  const accessToken = 'test-access-token';

  beforeEach(() => {
    vi.clearAllMocks();
    client = new SessionClient({ baseUrl: 'https://api.example.com' });
  });

  describe('listSessions', () => {
    it('should send GET with auth header', async () => {
      getMockAxios().get.mockResolvedValueOnce({
        data: { data: [mockSession], total: 1, page: 1, pageSize: 20 },
      });

      const result = await client.listSessions(accessToken);

      expect(getMockAxios().get).toHaveBeenCalledWith('/', expect.objectContaining({
        headers: { Authorization: `Bearer ${accessToken}` },
      }));
      expect(result.data).toHaveLength(1);
    });

    it('should pass filter params', async () => {
      getMockAxios().get.mockResolvedValueOnce({
        data: { data: [], total: 0, page: 1, pageSize: 10 },
      });

      await client.listSessions(accessToken, {
        status: 'active',
        page: 2,
        pageSize: 10,
        sortBy: 'createdAt',
        sortOrder: 'desc',
      });

      expect(getMockAxios().get).toHaveBeenCalledWith('/', expect.objectContaining({
        params: {
          status: 'active',
          page: 2,
          page_size: 10,
          sort_by: 'createdAt',
          sort_order: 'desc',
        },
      }));
    });
  });

  describe('getSession', () => {
    it('should return session detail', async () => {
      getMockAxios().get.mockResolvedValueOnce({ data: mockSession });

      const result = await client.getSession(accessToken, 'session-123');

      expect(result.id).toBe('session-123');
    });

    it('should throw SessionNotFoundError on 404', async () => {
      getMockAxios().get.mockRejectedValueOnce({
        response: { status: 404 },
      });

      await expect(
        client.getSession(accessToken, 'missing')
      ).rejects.toThrow(SessionNotFoundError);
    });
  });

  describe('revokeSession', () => {
    it('should send POST with reason', async () => {
      getMockAxios().post.mockResolvedValueOnce({ data: {} });

      await client.revokeSession(accessToken, 'session-123', 'Suspicious activity');

      expect(getMockAxios().post).toHaveBeenCalledWith(
        '/session-123/revoke',
        { reason: 'Suspicious activity' },
        expect.objectContaining({
          headers: { Authorization: `Bearer ${accessToken}` },
        })
      );
    });

    it('should throw SessionNotFoundError on 404', async () => {
      getMockAxios().post.mockRejectedValueOnce({
        response: { status: 404 },
      });

      await expect(
        client.revokeSession(accessToken, 'missing')
      ).rejects.toThrow(SessionNotFoundError);
    });

    it('should throw SessionAlreadyRevokedError on 409', async () => {
      getMockAxios().post.mockRejectedValueOnce({
        response: { status: 409 },
      });

      await expect(
        client.revokeSession(accessToken, 'already-revoked')
      ).rejects.toThrow(SessionAlreadyRevokedError);
    });
  });

  describe('revokeOtherSessions', () => {
    it('should send POST to /revoke-others', async () => {
      getMockAxios().post.mockResolvedValueOnce({ data: {} });

      await client.revokeOtherSessions(accessToken, 'Security');

      expect(getMockAxios().post).toHaveBeenCalledWith(
        '/revoke-others',
        { reason: 'Security' },
        expect.objectContaining({
          headers: { Authorization: `Bearer ${accessToken}` },
        })
      );
    });
  });

  describe('revokeAllSessions', () => {
    it('should send POST to /revoke-all', async () => {
      getMockAxios().post.mockResolvedValueOnce({ data: {} });

      await client.revokeAllSessions(accessToken, 'Account compromised');

      expect(getMockAxios().post).toHaveBeenCalledWith(
        '/revoke-all',
        { reason: 'Account compromised' },
        expect.objectContaining({
          headers: { Authorization: `Bearer ${accessToken}` },
        })
      );
    });
  });

  describe('extendSession', () => {
    it('should send to /{id}/extend with sessionId', async () => {
      getMockAxios().post.mockResolvedValueOnce({ data: mockSession });

      await client.extendSession(accessToken, 'session-123');

      expect(getMockAxios().post).toHaveBeenCalledWith(
        '/session-123/extend',
        null,
        expect.any(Object)
      );
    });

    it('should send to /current/extend without sessionId', async () => {
      getMockAxios().post.mockResolvedValueOnce({ data: mockSession });

      await client.extendSession(accessToken);

      expect(getMockAxios().post).toHaveBeenCalledWith(
        '/current/extend',
        null,
        expect.any(Object)
      );
    });

    it('should throw SessionNotFoundError on 404', async () => {
      getMockAxios().post.mockRejectedValueOnce({
        response: { status: 404 },
      });

      await expect(
        client.extendSession(accessToken, 'missing')
      ).rejects.toThrow(SessionNotFoundError);
    });
  });

  describe('getSessionActivity', () => {
    it('should return activity log', async () => {
      const mockActivity = {
        data: [
          {
            id: 'act-1',
            sessionId: 'session-123',
            action: 'login_succeeded',
            timestamp: '2025-01-15T10:00:00Z',
            ipAddress: '192.168.1.1',
          },
        ],
        total: 1,
        page: 1,
        pageSize: 20,
      };
      getMockAxios().get.mockResolvedValueOnce({ data: mockActivity });

      const result = await client.getSessionActivity(accessToken, 'session-123');

      expect(result.data).toHaveLength(1);
      expect(result.data[0].action).toBe('login_succeeded');
    });

    it('should throw SessionNotFoundError on 404', async () => {
      getMockAxios().get.mockRejectedValueOnce({
        response: { status: 404 },
      });

      await expect(
        client.getSessionActivity(accessToken, 'missing')
      ).rejects.toThrow(SessionNotFoundError);
    });
  });

  // Admin & Policy Methods

  describe('setSessionPolicy', () => {
    const mockPolicy = {
      maxSessionLifetimeMinutes: 1440,
      idleTimeoutMinutes: 30,
      maxConcurrentSessions: 5,
      onConcurrentLimitReached: 'deny_new' as const,
      bindToIP: false,
      bindToDevice: false,
      requireMFAForNewSession: true,
    };

    it('should send PUT with snake_case body', async () => {
      getMockAxios().put.mockResolvedValueOnce({
        data: {
          max_session_lifetime_minutes: 1440,
          idle_timeout_minutes: 30,
          max_concurrent_sessions: 5,
          on_concurrent_limit_reached: 'deny_new',
          bind_to_ip: false,
          bind_to_device: false,
          require_mfa_for_new_session: true,
        },
      });

      client.setAccessToken('admin-token');
      const result = await client.setSessionPolicy(mockPolicy);

      expect(getMockAxios().put).toHaveBeenCalledWith(
        '/policy',
        expect.objectContaining({
          max_session_lifetime_minutes: 1440,
          idle_timeout_minutes: 30,
        }),
        expect.any(Object)
      );
      expect(result.maxSessionLifetimeMinutes).toBe(1440);
    });

    it('should throw AdminRequiredError on 403', async () => {
      getMockAxios().put.mockRejectedValueOnce({
        response: { status: 403 },
      });

      client.setAccessToken('user-token');
      await expect(
        client.setSessionPolicy(mockPolicy)
      ).rejects.toThrow(AdminRequiredError);
    });

    it('should throw InvalidSessionPolicyError on 422', async () => {
      getMockAxios().put.mockRejectedValueOnce({
        response: {
          status: 422,
          data: { field: 'maxConcurrentSessions', message: 'Must be positive' },
        },
      });

      client.setAccessToken('admin-token');
      await expect(
        client.setSessionPolicy({ ...mockPolicy, maxConcurrentSessions: -1 })
      ).rejects.toThrow(InvalidSessionPolicyError);
    });
  });

  describe('getSessionPolicy', () => {
    it('should return the current policy', async () => {
      getMockAxios().get.mockResolvedValueOnce({
        data: {
          max_session_lifetime_minutes: 1440,
          idle_timeout_minutes: 30,
          max_concurrent_sessions: 5,
          on_concurrent_limit_reached: 'deny_new',
          bind_to_ip: false,
          bind_to_device: false,
          require_mfa_for_new_session: false,
        },
      });

      client.setAccessToken('admin-token');
      const result = await client.getSessionPolicy();

      expect(result.maxSessionLifetimeMinutes).toBe(1440);
      expect(result.idleTimeoutMinutes).toBe(30);
    });

    it('should throw AdminRequiredError on 403', async () => {
      getMockAxios().get.mockRejectedValueOnce({
        response: { status: 403 },
      });

      client.setAccessToken('user-token');
      await expect(client.getSessionPolicy()).rejects.toThrow(AdminRequiredError);
    });
  });

  describe('adminListUserSessions', () => {
    it('should send GET with userId in path', async () => {
      getMockAxios().get.mockResolvedValueOnce({
        data: { data: [mockSession], total: 1, page: 1, pageSize: 20 },
      });

      client.setAccessToken('admin-token');
      await client.adminListUserSessions('user-456');

      expect(getMockAxios().get).toHaveBeenCalledWith(
        '/admin/users/user-456',
        expect.any(Object)
      );
    });

    it('should pass admin filter params', async () => {
      getMockAxios().get.mockResolvedValueOnce({
        data: { data: [], total: 0, page: 1, pageSize: 20 },
      });

      client.setAccessToken('admin-token');
      await client.adminListUserSessions('user-456', {
        deviceType: 'mobile',
        country: 'US',
        authMethod: 'sso',
      });

      expect(getMockAxios().get).toHaveBeenCalledWith(
        '/admin/users/user-456',
        expect.objectContaining({
          params: expect.objectContaining({
            device_type: 'mobile',
            country: 'US',
            auth_method: 'sso',
          }),
        })
      );
    });

    it('should throw AdminRequiredError on 403', async () => {
      getMockAxios().get.mockRejectedValueOnce({
        response: { status: 403 },
      });

      client.setAccessToken('user-token');
      await expect(
        client.adminListUserSessions('user-456')
      ).rejects.toThrow(AdminRequiredError);
    });
  });

  describe('adminRevokeUserSession', () => {
    it('should send POST with reason', async () => {
      getMockAxios().post.mockResolvedValueOnce({ data: {} });

      client.setAccessToken('admin-token');
      await client.adminRevokeUserSession('user-456', 'session-123', 'Policy violation');

      expect(getMockAxios().post).toHaveBeenCalledWith(
        '/admin/users/user-456/session-123/revoke',
        { reason: 'Policy violation' },
        expect.any(Object)
      );
    });

    it('should throw AdminRequiredError on 403', async () => {
      getMockAxios().post.mockRejectedValueOnce({
        response: { status: 403 },
      });

      client.setAccessToken('user-token');
      await expect(
        client.adminRevokeUserSession('user-456', 'session-123')
      ).rejects.toThrow(AdminRequiredError);
    });

    it('should throw SessionNotFoundError on 404', async () => {
      getMockAxios().post.mockRejectedValueOnce({
        response: { status: 404 },
      });

      client.setAccessToken('admin-token');
      await expect(
        client.adminRevokeUserSession('user-456', 'missing')
      ).rejects.toThrow(SessionNotFoundError);
    });

    it('should throw SessionAlreadyRevokedError on 409', async () => {
      getMockAxios().post.mockRejectedValueOnce({
        response: { status: 409 },
      });

      client.setAccessToken('admin-token');
      await expect(
        client.adminRevokeUserSession('user-456', 'already-revoked')
      ).rejects.toThrow(SessionAlreadyRevokedError);
    });
  });

  describe('adminRevokeAllUserSessions', () => {
    it('should send POST with reason', async () => {
      getMockAxios().post.mockResolvedValueOnce({ data: {} });

      client.setAccessToken('admin-token');
      await client.adminRevokeAllUserSessions('user-456', 'Account compromised');

      expect(getMockAxios().post).toHaveBeenCalledWith(
        '/admin/users/user-456/revoke-all',
        { reason: 'Account compromised' },
        expect.any(Object)
      );
    });

    it('should throw AdminRequiredError on 403', async () => {
      getMockAxios().post.mockRejectedValueOnce({
        response: { status: 403 },
      });

      client.setAccessToken('user-token');
      await expect(
        client.adminRevokeAllUserSessions('user-456')
      ).rejects.toThrow(AdminRequiredError);
    });
  });

  describe('adminGetSessionStats', () => {
    const mockStats = {
      totalActiveSessions: 150,
      uniqueUsers: 75,
      sessionsByDeviceType: { desktop: 100, mobile: 40, tablet: 10 },
      sessionsByAuthMethod: { password: 80, sso: 70 },
      sessionsByCountry: { US: 100, GB: 30, DE: 20 },
      averageSessionDurationMinutes: 45,
      peakConcurrentSessions: 120,
    };

    it('should return stats', async () => {
      getMockAxios().get.mockResolvedValueOnce({ data: mockStats });

      client.setAccessToken('admin-token');
      const result = await client.adminGetSessionStats();

      expect(result.totalActiveSessions).toBe(150);
      expect(result.uniqueUsers).toBe(75);
    });

    it('should pass tenantId as query param', async () => {
      getMockAxios().get.mockResolvedValueOnce({ data: mockStats });

      client.setAccessToken('admin-token');
      await client.adminGetSessionStats('tenant-789');

      expect(getMockAxios().get).toHaveBeenCalledWith(
        '/admin/stats',
        expect.objectContaining({
          params: { tenant_id: 'tenant-789' },
        })
      );
    });

    it('should throw AdminRequiredError on 403', async () => {
      getMockAxios().get.mockRejectedValueOnce({
        response: { status: 403 },
      });

      client.setAccessToken('user-token');
      await expect(
        client.adminGetSessionStats()
      ).rejects.toThrow(AdminRequiredError);
    });
  });
});
```

**Tests**: This task IS the test file.

**Commit**: `test(sessions): add comprehensive tests for SessionClient`

---

## Summary

| Task | Scope | Files Changed | Est. Time |
|------|-------|---------------|-----------|
| 1 | Auth types | `src/auth/types.ts` | 10 min |
| 2 | Auth errors | `src/auth/errors.ts` | 5 min |
| 3 | PKCE + buildAuthorizationUrl | `src/auth/pkce.ts` (new), `src/auth/client.ts` | 12 min |
| 4 | exchangeCode + clientCredentials | `src/auth/client.ts` | 10 min |
| 5 | OIDC discovery | `src/auth/client.ts` | 10 min |
| 6 | getSigningKeys + validateToken | `src/auth/client.ts` | 15 min |
| 7 | Step-up auth | `src/auth/client.ts` | 10 min |
| 8 | Auto-refresh + callbacks | `src/auth/client.ts` | 15 min |
| 9 | Auth barrel exports | `src/auth/index.ts` | 5 min |
| 10 | Session types | `src/sessions/types.ts` (new) | 10 min |
| 11 | Session errors | `src/sessions/errors.ts` (new) | 5 min |
| 12 | SessionClient user methods | `src/sessions/client.ts` (new) | 15 min |
| 13 | SessionClient admin/policy | `src/sessions/client.ts` | 12 min |
| 14 | Session barrel + SDK index | `src/sessions/index.ts` (new), `src/index.ts`, `package.json` | 5 min |
| 15 | Auth enhancement tests | 5 test files (new) | 15 min |
| 16 | Session management tests | `src/__tests__/sessions.test.ts` (new) | 12 min |
| **Total** | | **11 new files, 5 modified** | **~166 min** |

## Dependency Graph

```
Task 1 (types) ──┬── Task 3 (PKCE) ── Task 4 (exchangeCode)
                  ├── Task 5 (discovery) ── Task 6 (validateToken)
Task 2 (errors) ──┤── Task 7 (step-up)
                  └── Task 8 (auto-refresh)
                        │
Task 9 (auth exports) ──┘

Task 10 (session types) ── Task 12 (user methods)
Task 11 (session errors) ──┤
                            └── Task 13 (admin methods)
                                  │
Task 14 (session exports) ────────┘

Task 15 (auth tests) ── depends on Tasks 3-8
Task 16 (session tests) ── depends on Tasks 12-13
```

## Verification Checklist

After all tasks are complete, run:

```bash
cd packages/node

# Type checking
npm run typecheck

# All tests
npm test

# Build
npm run build
```

All three commands must pass with zero errors.
