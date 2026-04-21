package com.auction.common.entity;

/**
 * Cấu hình đặt giá tự động — Bidder đặt maxBid, hệ thống tự bid dần.
 *
 * <p>Kế thừa từ {@link Entity}. Khi có bid mới từ người khác,
 * hệ thống tự động tăng giá lên (currentPrice + increment) nếu chưa vượt maxBid.
 *
 * <p>Sẽ được Người D triển khai logic trong AutoBidService (Tuần 13-14).
 * Người B chỉ tạo data model ở đây.
 */
public class AutoBid extends Entity {

    private static final long serialVersionUID = 1L;

    /** ID phiên đấu giá áp dụng auto-bid. */
    private String auctionId;

    /** ID người đặt auto-bid. */
    private String bidderId;

    /** Giá tối đa sẵn sàng trả (VND). */
    private double maxBid;

    /** Bước tăng giá mỗi lần (VND). */
    private double increment;

    /** Trạng thái: true = đang hoạt động, false = đã tắt. */
    private boolean active;

    /**
     * Constructor mặc định.
     */
    public AutoBid() {
        super();
        this.active = true;
    }

    /**
     * Constructor đầy đủ.
     *
     * @param auctionId ID phiên đấu giá
     * @param bidderId  ID người đặt
     * @param maxBid    giá tối đa
     * @param increment bước tăng giá
     */
    public AutoBid(String auctionId, String bidderId, double maxBid, double increment) {
        super();
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.maxBid = maxBid;
        this.increment = increment;
        this.active = true;
    }

    // ==================== Polymorphism ====================

    @Override
    public String getInfo() {
        return String.format("AutoBid[id=%s, auctionId=%s, bidderId=%s, maxBid=%.0f, increment=%.0f, active=%s]",
                getId(), auctionId, bidderId, maxBid, increment, active);
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

    public double getMaxBid() {
        return maxBid;
    }

    public void setMaxBid(double maxBid) {
        this.maxBid = maxBid;
    }

    public double getIncrement() {
        return increment;
    }

    public void setIncrement(double increment) {
        this.increment = increment;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
