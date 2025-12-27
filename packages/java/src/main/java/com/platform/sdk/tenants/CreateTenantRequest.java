package com.platform.sdk.tenants;

import java.util.Map;

/**
 * Request to create a new tenant.
 */
public class CreateTenantRequest {
    private String name;
    private String slug;
    private String domain;
    private String logoUrl;
    private String primaryColor;
    private SubscriptionPlan plan;
    private Tenant.PrimaryContact primaryContact;
    private String industry;
    private String companySize;
    private Map<String, Object> settings;
    private Map<String, Object> metadata;

    public CreateTenantRequest() {
    }

    public CreateTenantRequest name(String name) {
        this.name = name;
        return this;
    }

    public CreateTenantRequest slug(String slug) {
        this.slug = slug;
        return this;
    }

    public CreateTenantRequest domain(String domain) {
        this.domain = domain;
        return this;
    }

    public CreateTenantRequest logoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
        return this;
    }

    public CreateTenantRequest primaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
        return this;
    }

    public CreateTenantRequest plan(SubscriptionPlan plan) {
        this.plan = plan;
        return this;
    }

    public CreateTenantRequest primaryContact(Tenant.PrimaryContact primaryContact) {
        this.primaryContact = primaryContact;
        return this;
    }

    public CreateTenantRequest industry(String industry) {
        this.industry = industry;
        return this;
    }

    public CreateTenantRequest companySize(String companySize) {
        this.companySize = companySize;
        return this;
    }

    public CreateTenantRequest settings(Map<String, Object> settings) {
        this.settings = settings;
        return this;
    }

    public CreateTenantRequest metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
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

    public Tenant.PrimaryContact getPrimaryContact() {
        return primaryContact;
    }

    public String getIndustry() {
        return industry;
    }

    public String getCompanySize() {
        return companySize;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
