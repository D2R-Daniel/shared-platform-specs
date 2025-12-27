package com.platform.sdk.tenants;

import java.time.Instant;
import java.util.Map;

/**
 * Tenant/Organization model.
 */
public class Tenant {
    private String id;
    private String name;
    private String slug;
    private String domain;
    private String logoUrl;
    private String primaryColor;
    private TenantStatus status;
    private String statusReason;
    private SubscriptionPlan plan;
    private String billingCycle;
    private String contractEndDate;
    private PrimaryContact primaryContact;
    private Address address;
    private String industry;
    private String companySize;
    private TenantFeatures features;
    private Map<String, Object> settings;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private String createdBy;
    private String updatedBy;

    public Tenant() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public void setStatus(TenantStatus status) {
        this.status = status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public String getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(String contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public PrimaryContact getPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(PrimaryContact primaryContact) {
        this.primaryContact = primaryContact;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getCompanySize() {
        return companySize;
    }

    public void setCompanySize(String companySize) {
        this.companySize = companySize;
    }

    public TenantFeatures getFeatures() {
        return features;
    }

    public void setFeatures(TenantFeatures features) {
        this.features = features;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
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

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Primary contact information.
     */
    public static class PrimaryContact {
        private String name;
        private String email;
        private String phone;

        public PrimaryContact() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    /**
     * Address information.
     */
    public static class Address {
        private String street;
        private String city;
        private String state;
        private String country;
        private String postalCode;

        public Address() {
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }
    }

    /**
     * Tenant features configuration.
     */
    public static class TenantFeatures {
        private Boolean ssoEnabled;
        private Boolean scimEnabled;
        private Boolean customBrandingEnabled;
        private Boolean analyticsEnabled;
        private Integer maxUsers;
        private Integer maxStorageGb;

        public TenantFeatures() {
        }

        public Boolean getSsoEnabled() {
            return ssoEnabled;
        }

        public void setSsoEnabled(Boolean ssoEnabled) {
            this.ssoEnabled = ssoEnabled;
        }

        public Boolean getScimEnabled() {
            return scimEnabled;
        }

        public void setScimEnabled(Boolean scimEnabled) {
            this.scimEnabled = scimEnabled;
        }

        public Boolean getCustomBrandingEnabled() {
            return customBrandingEnabled;
        }

        public void setCustomBrandingEnabled(Boolean customBrandingEnabled) {
            this.customBrandingEnabled = customBrandingEnabled;
        }

        public Boolean getAnalyticsEnabled() {
            return analyticsEnabled;
        }

        public void setAnalyticsEnabled(Boolean analyticsEnabled) {
            this.analyticsEnabled = analyticsEnabled;
        }

        public Integer getMaxUsers() {
            return maxUsers;
        }

        public void setMaxUsers(Integer maxUsers) {
            this.maxUsers = maxUsers;
        }

        public Integer getMaxStorageGb() {
            return maxStorageGb;
        }

        public void setMaxStorageGb(Integer maxStorageGb) {
            this.maxStorageGb = maxStorageGb;
        }
    }
}
