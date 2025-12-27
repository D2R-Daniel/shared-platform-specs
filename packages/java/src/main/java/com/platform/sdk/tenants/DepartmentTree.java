package com.platform.sdk.tenants;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Department with children for tree representation.
 */
public class DepartmentTree {
    private String id;
    private String tenantId;
    private String name;
    private String code;
    private String description;
    private String parentId;
    private String path;
    private Integer level;
    private String headUserId;
    private Boolean isActive;
    private Integer sortOrder;
    private Map<String, Object> settings;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;
    private List<DepartmentTree> children;
    private Integer memberCount;
    private Integer totalMemberCount;

    public DepartmentTree() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getHeadUserId() {
        return headUserId;
    }

    public void setHeadUserId(String headUserId) {
        this.headUserId = headUserId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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

    public List<DepartmentTree> getChildren() {
        return children;
    }

    public void setChildren(List<DepartmentTree> children) {
        this.children = children;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getTotalMemberCount() {
        return totalMemberCount;
    }

    public void setTotalMemberCount(Integer totalMemberCount) {
        this.totalMemberCount = totalMemberCount;
    }
}
