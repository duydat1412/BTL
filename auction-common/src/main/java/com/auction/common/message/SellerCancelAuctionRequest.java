package com.auction.common.message;

import java.io.Serializable;

public class SellerCancelAuctionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String sellerId;
    private final String auctionId;
    private final String reason;

    public SellerCancelAuctionRequest(String sellerId, String auctionId, String reason) {
        this.sellerId = sellerId;
        this.auctionId = auctionId;
        this.reason = reason;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getReason() {
        return reason;
    }
}