package com.platform.sdk.tenants;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * SSO provider types.
 */
public enum SSOProvider {
    AZURE_AD("azure_ad"),
    OKTA("okta"),
    GOOGLE("google"),
    SAML("saml"),
    OIDC("oidc");

    private final String value;

    SSOProvider(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static SSOProvider fromValue(String value) {
        for (SSOProvider provider : values()) {
            if (provider.value.equals(value)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown SSO provider: " + value);
    }
}
