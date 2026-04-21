package com.auction.common.entity;

import com.auction.common.enums.UserRole;

/**
 * Người đặt giá — tham gia các phiên đấu giá.
 *
 * <p>Kế thừa từ {@link User}, role tự động gán là {@link UserRole#BIDDER}.
 * Thể hiện tính Đa hình (Polymorphism) qua override {@link #getInfo()}.
 */
public class Bidder extends User {

    private static final long serialVersionUID = 1L;

    /** Tổng số lần đặt giá. */
    private int totalBids;

    /** Số phiên đấu giá đã thắng. */
    private int wonAuctions;

    /**
     * Constructor mặc định.
     */
    public Bidder() {
        super();
        setRole(UserRole.BIDDER);
    }

    /**
     * Constructor đầy đủ.
     *
     * @param username tên đăng nhập
     * @param password mật khẩu
     * @param email    email
     */
    public Bidder(String username, String password, String email) {
        super(username, password, email, UserRole.BIDDER);
        this.totalBids = 0;
        this.wonAuctions = 0;
    }

    // ==================== Polymorphism ====================

    /**
     * Trả về thông tin chi tiết của Bidder.
     * Override từ Entity — thể hiện tính Đa hình.
     */
    @Override
    public String getInfo() {
        return String.format("Bidder[id=%s, username=%s, email=%s, totalBids=%d, wonAuctions=%d]",
                getId(), getUsername(), getEmail(), totalBids, wonAuctions);
    }

    // ==================== Getters & Setters ====================

    public int getTotalBids() {
        return totalBids;
    }

    public void setTotalBids(int totalBids) {
        this.totalBids = totalBids;
    }

    public int getWonAuctions() {
        return wonAuctions;
    }

    public void setWonAuctions(int wonAuctions) {
        this.wonAuctions = wonAuctions;
    }
}
