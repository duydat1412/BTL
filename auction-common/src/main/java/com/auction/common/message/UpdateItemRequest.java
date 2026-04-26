package com.auction.common.message;

import com.auction.common.enums.ItemType;
import java.io.Serializable;
import java.util.Map;

/**
 * Payload for UPDATE_ITEM action.
 */
public class UpdateItemRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String itemId;
    private final String name;
    private final String description;
    private final Double startingPrice;
    private final ItemType itemType;
    private final Map<String, String> extraAttributes;

    public UpdateItemRequest(String itemId, String name, String description, Double startingPrice,
                             ItemType itemType, Map<String, String> extraAttributes) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.itemType = itemType;
        this.extraAttributes = extraAttributes;
    }

    public String getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getStartingPrice() {
        return startingPrice;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public Map<String, String> getExtraAttributes() {
        return extraAttributes;
    }
}

