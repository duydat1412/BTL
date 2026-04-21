package com.auction.common.entity;

import com.auction.common.enums.AuctionStatus;
import java.time.LocalDateTime;

/**
 * Phiên đấu giá — liên kết 1 Item với các Bidder.
 *
 * <p>Kế thừa từ {@link Entity}. Quản lý trạng thái đấu giá,
 * giá hiện tại, người đặt giá cao nhất, thời gian bắt đầu/kết thúc.
 *
 * <p>Vòng đời: OPEN → RUNNING → FINISHED → PAID / CANCELED.
 */
public class Auction extends Entity {

    private static final long serialVersionUID = 1L;

    /** ID sản phẩm đang đấu giá. */
    private String itemId;

    /** ID người bán. */
    private String sellerId;

    /** Tiêu đề phiên đấu giá. */
    private String title;

    /** Giá khởi điểm (VND). */
    private double startPrice;

    /** Giá hiện tại (= giá bid cao nhất). */
    private double currentPrice;

    /** ID của người đặt giá cao nhất hiện tại. */
    private String highestBidderId;

    /** Trạng thái hiện tại của phiên đấu giá. */
    private AuctionStatus status;

    /** Thời gian bắt đầu phiên. */
    private LocalDateTime startTime;

    /** Thời gian kết thúc phiên. */
    private LocalDateTime endTime;

    /**
     * Constructor mặc định — trạng thái ban đầu là OPEN.
     */
    public Auction() {
        super();
        this.status = AuctionStatus.OPEN;
    }

    /**
     * Constructor đầy đủ.
     *
     * @param itemId     ID sản phẩm
     * @param sellerId   ID người bán
     * @param title      tiêu đề
     * @param startPrice giá khởi điểm
     * @param startTime  thời gian bắt đầu
     * @param endTime    thời gian kết thúc
     */
    public Auction(String itemId, String sellerId, String title,
                   double startPrice, LocalDateTime startTime, LocalDateTime endTime) {
        super();
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.title = title;
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.status = AuctionStatus.OPEN;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // ==================== Polymorphism ====================

    @Override
    public String getInfo() {
        return String.format(
                "Auction[id=%s, title=%s, status=%s, startPrice=%.0f, currentPrice=%.0f, highestBidder=%s]",
                getId(), title, status, startPrice, currentPrice, highestBidderId);
    }

    // ==================== Getters & Setters ====================

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getHighestBidderId() {
        return highestBidderId;
    }

    public void setHighestBidderId(String highestBidderId) {
        this.highestBidderId = highestBidderId;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
