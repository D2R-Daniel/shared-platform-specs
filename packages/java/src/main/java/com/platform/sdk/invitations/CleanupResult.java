package com.platform.sdk.invitations;

/**
 * Result of cleanup operation.
 */
public class CleanupResult {
    private Integer expiredCount;
    private Integer deletedCount;

    public CleanupResult() {
    }

    public Integer getExpiredCount() {
        return expiredCount;
    }

    public void setExpiredCount(Integer expiredCount) {
        this.expiredCount = expiredCount;
    }

    public Integer getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(Integer deletedCount) {
        this.deletedCount = deletedCount;
    }
}
