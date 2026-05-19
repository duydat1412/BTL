package com.auction.common.message;

import java.io.Serializable;

public class UnbanUserRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String adminId;
    private final String targetUserId;
    private final String reason;

    public UnbanUserRequest(String adminId, String targetUserId, String reason) {
        this.adminId = adminId;
        this.targetUserId = targetUserId;
        this.reason = reason;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public String getReason() {
        return reason;
    }
}
