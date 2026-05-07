package com.auction.common.message;

import java.io.Serializable;

/**
 * Payload for PLACE_BID action.
 */
public class PlaceBidRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String bidderId;
    private final double amount;
    private final boolean isAutoBid;

    public PlaceBidRequest(String auctionId, String bidderId, double amount, boolean isAutoBid) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.isAutoBid = isAutoBid;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isAutoBid() {
        return isAutoBid;
    }
}
