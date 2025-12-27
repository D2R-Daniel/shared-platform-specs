package com.platform.sdk.tenants;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * SSO configuration for a tenant.
 */
public class SSOConfig {
    private String id;
    private String tenantId;
    private SSOProvider provider;
    private Boolean enabled;
    private String displayName;

    // Provider-specific configs
    private AzureADConfig azureAd;
    private OktaConfig okta;
    private GoogleConfig google;
    private SAMLConfig saml;
    private OIDCConfig oidc;

    // SCIM and JIT
    private SCIMConfig scim;
    private JITProvisioningConfig jitProvisioning;

    // Attribute mappings
    private Map<String, String> attributeMappings;

    // Sync settings
    private Instant lastSyncAt;
    private String syncFrequency;

    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;

    public SSOConfig() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public SSOProvider getProvider() {
        return provider;
    }

    public void setProvider(SSOProvider provider) {
        this.provider = provider;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public AzureADConfig getAzureAd() {
        return azureAd;
    }

    public void setAzureAd(AzureADConfig azureAd) {
        this.azureAd = azureAd;
    }

    public OktaConfig getOkta() {
        return okta;
    }

    public void setOkta(OktaConfig okta) {
        this.okta = okta;
    }

    public GoogleConfig getGoogle() {
        return google;
    }

    public void setGoogle(GoogleConfig google) {
        this.google = google;
    }

    public SAMLConfig getSaml() {
        return saml;
    }

    public void setSaml(SAMLConfig saml) {
        this.saml = saml;
    }

    public OIDCConfig getOidc() {
        return oidc;
    }

    public void setOidc(OIDCConfig oidc) {
        this.oidc = oidc;
    }

    public SCIMConfig getScim() {
        return scim;
    }

    public void setScim(SCIMConfig scim) {
        this.scim = scim;
    }

    public JITProvisioningConfig getJitProvisioning() {
        return jitProvisioning;
    }

    public void setJitProvisioning(JITProvisioningConfig jitProvisioning) {
        this.jitProvisioning = jitProvisioning;
    }

    public Map<String, String> getAttributeMappings() {
        return attributeMappings;
    }

    public void setAttributeMappings(Map<String, String> attributeMappings) {
        this.attributeMappings = attributeMappings;
    }

    public Instant getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(Instant lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public String getSyncFrequency() {
        return syncFrequency;
    }

    public void setSyncFrequency(String syncFrequency) {
        this.syncFrequency = syncFrequency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Azure AD configuration.
     */
    public static class AzureADConfig {
        private String tenantId;
        private String clientId;
        private String clientSecretEncrypted;
        private String discoveryUrl;

        public AzureADConfig() {
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecretEncrypted() {
            return clientSecretEncrypted;
        }

        public void setClientSecretEncrypted(String clientSecretEncrypted) {
            this.clientSecretEncrypted = clientSecretEncrypted;
        }

        public String getDiscoveryUrl() {
            return discoveryUrl;
        }

        public void setDiscoveryUrl(String discoveryUrl) {
            this.discoveryUrl = discoveryUrl;
        }
    }

    /**
     * Okta configuration.
     */
    public static class OktaConfig {
        private String domain;
        private String clientId;
        private String clientSecretEncrypted;
        private String authorizationServer;

        public OktaConfig() {
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecretEncrypted() {
            return clientSecretEncrypted;
        }

        public void setClientSecretEncrypted(String clientSecretEncrypted) {
            this.clientSecretEncrypted = clientSecretEncrypted;
        }

        public String getAuthorizationServer() {
            return authorizationServer;
        }

        public void setAuthorizationServer(String authorizationServer) {
            this.authorizationServer = authorizationServer;
        }
    }

    /**
     * Google SSO configuration.
     */
    public static class GoogleConfig {
        private String clientId;
        private String clientSecretEncrypted;
        private String hostedDomain;

        public GoogleConfig() {
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecretEncrypted() {
            return clientSecretEncrypted;
        }

        public void setClientSecretEncrypted(String clientSecretEncrypted) {
            this.clientSecretEncrypted = clientSecretEncrypted;
        }

        public String getHostedDomain() {
            return hostedDomain;
        }

        public void setHostedDomain(String hostedDomain) {
            this.hostedDomain = hostedDomain;
        }
    }

    /**
     * SAML configuration.
     */
    public static class SAMLConfig {
        private String metadataUrl;
        private String entityId;
        private String ssoUrl;
        private String sloUrl;
        private String certificate;
        private String signatureAlgorithm;
        private String nameIdFormat;

        public SAMLConfig() {
        }

        public String getMetadataUrl() {
            return metadataUrl;
        }

        public void setMetadataUrl(String metadataUrl) {
            this.metadataUrl = metadataUrl;
        }

        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        public String getSsoUrl() {
            return ssoUrl;
        }

        public void setSsoUrl(String ssoUrl) {
            this.ssoUrl = ssoUrl;
        }

        public String getSloUrl() {
            return sloUrl;
        }

        public void setSloUrl(String sloUrl) {
            this.sloUrl = sloUrl;
        }

        public String getCertificate() {
            return certificate;
        }

        public void setCertificate(String certificate) {
            this.certificate = certificate;
        }

        public String getSignatureAlgorithm() {
            return signatureAlgorithm;
        }

        public void setSignatureAlgorithm(String signatureAlgorithm) {
            this.signatureAlgorithm = signatureAlgorithm;
        }

        public String getNameIdFormat() {
            return nameIdFormat;
        }

        public void setNameIdFormat(String nameIdFormat) {
            this.nameIdFormat = nameIdFormat;
        }
    }

    /**
     * OIDC configuration.
     */
    public static class OIDCConfig {
        private String issuer;
        private String clientId;
        private String clientSecretEncrypted;
        private String authorizationEndpoint;
        private String tokenEndpoint;
        private String userinfoEndpoint;
        private String jwksUri;
        private List<String> scopes;

        public OIDCConfig() {
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecretEncrypted() {
            return clientSecretEncrypted;
        }

        public void setClientSecretEncrypted(String clientSecretEncrypted) {
            this.clientSecretEncrypted = clientSecretEncrypted;
        }

        public String getAuthorizationEndpoint() {
            return authorizationEndpoint;
        }

        public void setAuthorizationEndpoint(String authorizationEndpoint) {
            this.authorizationEndpoint = authorizationEndpoint;
        }

        public String getTokenEndpoint() {
            return tokenEndpoint;
        }

        public void setTokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
        }

        public String getUserinfoEndpoint() {
            return userinfoEndpoint;
        }

        public void setUserinfoEndpoint(String userinfoEndpoint) {
            this.userinfoEndpoint = userinfoEndpoint;
        }

        public String getJwksUri() {
            return jwksUri;
        }

        public void setJwksUri(String jwksUri) {
            this.jwksUri = jwksUri;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void setScopes(List<String> scopes) {
            this.scopes = scopes;
        }
    }

    /**
     * SCIM configuration.
     */
    public static class SCIMConfig {
        private Boolean enabled;
        private String bearerToken;
        private String baseUrl;
        private Boolean syncGroups;

        public SCIMConfig() {
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getBearerToken() {
            return bearerToken;
        }

        public void setBearerToken(String bearerToken) {
            this.bearerToken = bearerToken;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Boolean getSyncGroups() {
            return syncGroups;
        }

        public void setSyncGroups(Boolean syncGroups) {
            this.syncGroups = syncGroups;
        }
    }

    /**
     * JIT (Just-In-Time) provisioning configuration.
     */
    public static class JITProvisioningConfig {
        private Boolean enabled;
        private String defaultRoleId;
        private String defaultDepartmentId;
        private Boolean requireEmailDomain;

        public JITProvisioningConfig() {
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getDefaultRoleId() {
            return defaultRoleId;
        }

        public void setDefaultRoleId(String defaultRoleId) {
            this.defaultRoleId = defaultRoleId;
        }

        public String getDefaultDepartmentId() {
            return defaultDepartmentId;
        }

        public void setDefaultDepartmentId(String defaultDepartmentId) {
            this.defaultDepartmentId = defaultDepartmentId;
        }

        public Boolean getRequireEmailDomain() {
            return requireEmailDomain;
        }

        public void setRequireEmailDomain(Boolean requireEmailDomain) {
            this.requireEmailDomain = requireEmailDomain;
        }
    }
}
