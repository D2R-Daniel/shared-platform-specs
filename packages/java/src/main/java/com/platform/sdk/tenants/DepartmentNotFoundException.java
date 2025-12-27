package com.platform.sdk.tenants;

/**
 * Exception thrown when a department is not found.
 */
public class DepartmentNotFoundException extends TenantException {
    private final String departmentId;

    public DepartmentNotFoundException(String departmentId) {
        super("Department not found: " + departmentId, 404, "DEPARTMENT_NOT_FOUND");
        this.departmentId = departmentId;
    }

    public String getDepartmentId() {
        return departmentId;
    }
}
