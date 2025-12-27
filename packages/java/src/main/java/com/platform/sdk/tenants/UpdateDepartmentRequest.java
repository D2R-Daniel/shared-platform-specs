package com.platform.sdk.tenants;

import java.util.Map;

/**
 * Request to update an existing department.
 */
public class UpdateDepartmentRequest {
    private String name;
    private String description;
    private String headUserId;
    private Boolean isActive;
    private Integer sortOrder;
    private String locationId;
    private String costCenter;
    private Map<String, Object> settings;
    private Map<String, Object> metadata;

    public UpdateDepartmentRequest() {
    }

    public UpdateDepartmentRequest name(String name) {
        this.name = name;
        return this;
    }

    public UpdateDepartmentRequest description(String description) {
        this.description = description;
        return this;
    }

    public UpdateDepartmentRequest headUserId(String headUserId) {
        this.headUserId = headUserId;
        return this;
    }

    public UpdateDepartmentRequest isActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public UpdateDepartmentRequest sortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public UpdateDepartmentRequest locationId(String locationId) {
        this.locationId = locationId;
        return this;
    }

    public UpdateDepartmentRequest costCenter(String costCenter) {
        this.costCenter = costCenter;
        return this;
    }

    public UpdateDepartmentRequest settings(Map<String, Object> settings) {
        this.settings = settings;
        return this;
    }

    public UpdateDepartmentRequest metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHeadUserId() {
        return headUserId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
