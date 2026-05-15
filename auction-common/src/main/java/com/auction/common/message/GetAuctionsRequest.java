package com.auction.common.message;

import java.io.Serializable;

/**
 * Payload cho lệnh GET_AUCTIONS.
 * Có thể lọc theo sellerId hoặc status. Để null nếu muốn lấy tất cả.
 */
public class GetAuctionsRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String sellerId;
    private final String statusFilter;

    public GetAuctionsRequest(String sellerId, String statusFilter) {
        this.sellerId = sellerId;
        this.statusFilter = statusFilter;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getStatusFilter() {
        return statusFilter;
    }
}
