package com.auction.common.entity;

import java.time.LocalDateTime;

/**
 * Giao dịch đặt giá — ghi lại mỗi lần Bidder đặt giá trong phiên đấu giá.
 *
 * <p>Kế thừa từ {@link Entity}. Mỗi BidTransaction liên kết 1 Auction
 * với 1 Bidder tại 1 thời điểm cụ thể.
 */
public class BidTransaction extends Entity {

    private static final long serialVersionUID = 1L;

    /** ID phiên đấu giá. */
    private String auctionId;

    /** ID người đặt giá. */
    private String bidderId;

    /** Số tiền đặt giá (VND). */
    private double bidAmount;

    /** Thời điểm đặt giá. */
    private LocalDateTime bidTime;

    /**
     * Constructor mặc định.
     */
    public BidTransaction() {
        super();
        this.bidTime = LocalDateTime.now();
    }

    /**
     * Constructor đầy đủ.
     *
     * @param auctionId ID phiên đấu giá
     * @param bidderId  ID người đặt giá
     * @param bidAmount số tiền đặt
     */
    public BidTransaction(String auctionId, String bidderId, double bidAmount) {
        super();
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidTime = LocalDateTime.now();
    }

    // ==================== Polymorphism ====================

    @Override
    public String getInfo() {
        return String.format("BidTransaction[id=%s, auctionId=%s, bidderId=%s, amount=%.0f, time=%s]",
                getId(), auctionId, bidderId, bidAmount, bidTime);
    }

    // ==================== Getters & Setters ====================

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public void setBidderId(String bidderId) {
        this.bidderId = bidderId;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }
}
