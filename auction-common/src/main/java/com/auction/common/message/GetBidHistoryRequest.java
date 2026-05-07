package com.auction.common.message;

import java.io.Serializable;

/**
 * Payload for GET_BID_HISTORY action.
 */
public class GetBidHistoryRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;

    public GetBidHistoryRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
