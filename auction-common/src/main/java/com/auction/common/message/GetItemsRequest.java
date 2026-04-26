package com.auction.common.message;

import com.auction.common.enums.ItemType;
import java.io.Serializable;

/**
 * Payload for GET_ITEMS action.
 */
public class GetItemsRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String sellerId;
    private final ItemType itemType;

    public GetItemsRequest(String sellerId, ItemType itemType) {
        this.sellerId = sellerId;
        this.itemType = itemType;
    }

    public String getSellerId() {
        return sellerId;
    }

    public ItemType getItemType() {
        return itemType;
    }
}

