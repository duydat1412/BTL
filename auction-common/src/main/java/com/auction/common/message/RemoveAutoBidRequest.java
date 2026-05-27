package com.auction.common.message;

import java.io.Serializable;

public class RemoveAutoBidRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String bidderId;

    public RemoveAutoBidRequest(String auctionId, String bidderId) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
    }

    public String getAuctionId() { return auctionId; }
    public String getBidderId() { return bidderId; }
}
