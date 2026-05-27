package com.auction.common.message;

import java.io.Serializable;

public class RegisterAutoBidRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String bidderId;
    private final double maxBid;
    private final double increment;

    public RegisterAutoBidRequest(String auctionId, String bidderId, double maxBid, double increment) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.maxBid = maxBid;
        this.increment = increment;
    }

    public String getAuctionId() { return auctionId; }
    public String getBidderId() { return bidderId; }
    public double getMaxBid() { return maxBid; }
    public double getIncrement() { return increment; }
}
