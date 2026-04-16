package com.auction.common.entity;

import java.io.Serializable;

/**
 * Lớp đại diện cho giao dịch Đặt giá.
 * (Sẽ được Người B hoàn thiện chi tiết sau).
 */
public class BidTransaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
