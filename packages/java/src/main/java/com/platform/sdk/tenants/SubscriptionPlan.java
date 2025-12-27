package com.platform.sdk.tenants;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Subscription plan types.
 */
public enum SubscriptionPlan {
    FREE("free"),
    BASIC("basic"),
    PRO("pro"),
    ENTERPRISE("enterprise");

    private final String value;

    SubscriptionPlan(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static SubscriptionPlan fromValue(String value) {
        for (SubscriptionPlan plan : values()) {
            if (plan.value.equals(value)) {
                return plan;
            }
        }
        throw new IllegalArgumentException("Unknown subscription plan: " + value);
    }
}
