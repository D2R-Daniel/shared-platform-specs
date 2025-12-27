package com.platform.sdk.permissions;

/**
 * Summary view of a role.
 */
public class RoleSummary {
    private String id;
    private String name;
    private String slug;
    private String description;
    private Integer hierarchyLevel;
    private Boolean isSystem;
    private Boolean isActive;
    private Integer permissionCount;

    public RoleSummary() {
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getHierarchyLevel() {
        return hierarchyLevel;
    }

    public void setHierarchyLevel(Integer hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getPermissionCount() {
        return permissionCount;
    }

    public void setPermissionCount(Integer permissionCount) {
        this.permissionCount = permissionCount;
    }
}
