package com.platform.sdk.tenants;

import java.util.Map;

/**
 * Request to create a new department.
 */
public class CreateDepartmentRequest {
    private String name;
    private String code;
    private String description;
    private String parentId;
    private String headUserId;
    private Boolean isActive;
    private Integer sortOrder;
    private String locationId;
    private String costCenter;
    private Map<String, Object> settings;
    private Map<String, Object> metadata;

    public CreateDepartmentRequest() {
    }

    public CreateDepartmentRequest name(String name) {
        this.name = name;
        return this;
    }

    public CreateDepartmentRequest code(String code) {
        this.code = code;
        return this;
    }

    public CreateDepartmentRequest description(String description) {
        this.description = description;
        return this;
    }

    public CreateDepartmentRequest parentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public CreateDepartmentRequest headUserId(String headUserId) {
        this.headUserId = headUserId;
        return this;
    }

    public CreateDepartmentRequest isActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public CreateDepartmentRequest sortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public CreateDepartmentRequest locationId(String locationId) {
        this.locationId = locationId;
        return this;
    }

    public CreateDepartmentRequest costCenter(String costCenter) {
        this.costCenter = costCenter;
        return this;
    }

    public CreateDepartmentRequest settings(Map<String, Object> settings) {
        this.settings = settings;
        return this;
    }

    public CreateDepartmentRequest metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getParentId() {
        return parentId;
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
