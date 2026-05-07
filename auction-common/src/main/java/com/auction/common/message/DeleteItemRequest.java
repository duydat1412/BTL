package com.auction.common.message;

import java.io.Serializable;

/**
 * Payload for DELETE_ITEM action.
 */
public class DeleteItemRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String itemId;
    private final String sellerId;

    public DeleteItemRequest(String itemId, String sellerId) {
        this.itemId = itemId;
        this.sellerId = sellerId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getSellerId() {
        return sellerId;
    }
}

