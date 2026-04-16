package com.auction.common.entity;

import java.io.Serializable;

/**
 * Lớp đại diện cho một Phiên đấu giá.
 * (Sẽ được Người B hoàn thiện chi tiết sau).
 */
public class Auction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String itemId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
}
