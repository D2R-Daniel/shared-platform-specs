package com.platform.sdk.tenants;

import java.util.Map;

/**
 * Request to update an existing tenant.
 */
public class UpdateTenantRequest {
    private String name;
    private String domain;
    private String logoUrl;
    private String primaryColor;
    private SubscriptionPlan plan;
    private String billingCycle;
    private Tenant.PrimaryContact primaryContact;
    private String industry;
    private String companySize;
    private Tenant.TenantFeatures features;
    private Map<String, Object> settings;
    private Map<String, Object> metadata;

    public UpdateTenantRequest() {
    }

    public UpdateTenantRequest name(String name) {
        this.name = name;
        return this;
    }

    public UpdateTenantRequest domain(String domain) {
        this.domain = domain;
        return this;
    }

    public UpdateTenantRequest logoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
        return this;
    }

    public UpdateTenantRequest primaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
        return this;
    }

    public UpdateTenantRequest plan(SubscriptionPlan plan) {
        this.plan = plan;
        return this;
    }

    public UpdateTenantRequest billingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
        return this;
    }

    public UpdateTenantRequest primaryContact(Tenant.PrimaryContact primaryContact) {
        this.primaryContact = primaryContact;
        return this;
    }

    public UpdateTenantRequest industry(String industry) {
        this.industry = industry;
        return this;
    }

    public UpdateTenantRequest companySize(String companySize) {
        this.companySize = companySize;
        return this;
    }

    public UpdateTenantRequest features(Tenant.TenantFeatures features) {
        this.features = features;
        return this;
    }

    public UpdateTenantRequest settings(Map<String, Object> settings) {
        this.settings = settings;
        return this;
    }

    public UpdateTenantRequest metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDomain() {
        return domain;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public Tenant.PrimaryContact getPrimaryContact() {
        return primaryContact;
    }

    public String getIndustry() {
        return industry;
    }

    public String getCompanySize() {
        return companySize;
    }

    public Tenant.TenantFeatures getFeatures() {
        return features;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
