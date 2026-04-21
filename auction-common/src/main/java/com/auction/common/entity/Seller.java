package com.auction.common.entity;

import com.auction.common.enums.UserRole;

/**
 * Người bán — đăng sản phẩm lên đấu giá.
 *
 * <p>Kế thừa từ {@link User}, role tự động gán là {@link UserRole#SELLER}.
 * Thể hiện tính Đa hình (Polymorphism) qua override {@link #getInfo()}.
 */
public class Seller extends User {

    private static final long serialVersionUID = 1L;

    /** Tổng số sản phẩm đã đăng bán. */
    private int totalListings;

    /** Điểm đánh giá trung bình (0.0 - 5.0). */
    private double rating;

    /**
     * Constructor mặc định.
     */
    public Seller() {
        super();
        setRole(UserRole.SELLER);
    }

    /**
     * Constructor đầy đủ.
     *
     * @param username tên đăng nhập
     * @param password mật khẩu
     * @param email    email
     */
    public Seller(String username, String password, String email) {
        super(username, password, email, UserRole.SELLER);
        this.totalListings = 0;
        this.rating = 0.0;
    }

    // ==================== Polymorphism ====================

    @Override
    public String getInfo() {
        return String.format("Seller[id=%s, username=%s, email=%s, totalListings=%d, rating=%.1f]",
                getId(), getUsername(), getEmail(), totalListings, rating);
    }

    // ==================== Getters & Setters ====================

    public int getTotalListings() {
        return totalListings;
    }

    public void setTotalListings(int totalListings) {
        this.totalListings = totalListings;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
