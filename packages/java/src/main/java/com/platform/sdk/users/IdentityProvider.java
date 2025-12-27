package com.platform.sdk.users;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Authentication provider types.
 */
public enum IdentityProvider {
    LOCAL("local"),
    GOOGLE("google"),
    MICROSOFT("microsoft"),
    OKTA("okta"),
    SAML("saml"),
    OIDC("oidc");

    private final String value;

    IdentityProvider(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static IdentityProvider fromValue(String value) {
        for (IdentityProvider provider : values()) {
            if (provider.value.equals(value)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown identity provider: " + value);
    }
}
