package com.auction.common.message;

import java.io.Serializable;

public class CancelAuctionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String adminId;
    private final String auctionId;
    private final String reason;

    public CancelAuctionRequest(String adminId, String auctionId, String reason) {
        this.adminId = adminId;
        this.auctionId = auctionId;
        this.reason = reason;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getReason() {
        return reason;
    }
}
