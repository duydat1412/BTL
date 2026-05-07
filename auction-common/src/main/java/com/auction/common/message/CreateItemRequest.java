package com.auction.common.message;

import com.auction.common.enums.ItemType;
import java.io.Serializable;
import java.util.Map;

/**
 * Payload for CREATE_ITEM action.
 */
public class CreateItemRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String description;
    private final double startingPrice;
    private final String sellerId;
    private final ItemType itemType;
    private final Map<String, String> extraAttributes;

    public CreateItemRequest(String name, String description, double startingPrice, String sellerId,
                             ItemType itemType, Map<String, String> extraAttributes) {
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.sellerId = sellerId;
        this.itemType = itemType;
        this.extraAttributes = extraAttributes;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public String getSellerId() {
        return sellerId;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public Map<String, String> getExtraAttributes() {
        return extraAttributes;
    }
}

