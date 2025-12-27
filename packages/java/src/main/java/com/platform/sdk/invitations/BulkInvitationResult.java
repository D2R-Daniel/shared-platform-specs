package com.platform.sdk.invitations;

import java.util.List;

/**
 * Result of bulk invitation creation.
 */
public class BulkInvitationResult {
    private List<InvitationSummary> successful;
    private List<BulkInvitationFailure> failed;
    private Integer total;
    private Integer successCount;
    private Integer failureCount;

    public BulkInvitationResult() {
    }

    public List<InvitationSummary> getSuccessful() {
        return successful;
    }

    public void setSuccessful(List<InvitationSummary> successful) {
        this.successful = successful;
    }

    public List<BulkInvitationFailure> getFailed() {
        return failed;
    }

    public void setFailed(List<BulkInvitationFailure> failed) {
        this.failed = failed;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    /**
     * A failed invitation in a bulk request.
     */
    public static class BulkInvitationFailure {
        private String email;
        private String reason;

        public BulkInvitationFailure() {
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
