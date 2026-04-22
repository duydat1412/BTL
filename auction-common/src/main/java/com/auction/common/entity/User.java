package com.auction.common.entity;

import com.auction.common.enums.UserRole;

/**
 * Lớp trừu tượng đại diện cho người dùng trong hệ thống.
 *
 * <p>Kế thừa từ {@link Entity}. Là lớp cha của {@link Bidder},
 * {@link Seller}, {@link Admin}.
 *
 * <p>Encapsulation: tất cả fields đều private, truy cập qua getter/setter.
 * Inheritance: các subclass kế thừa và mở rộng thêm thuộc tính riêng.
 */
public abstract class User extends Entity {

    private static final long serialVersionUID = 1L;

    /** Tên đăng nhập — duy nhất trong hệ thống. */
    private String username;

    /** Mật khẩu (đã hash bằng BCrypt — Người D xử lý). */
    private String password;

    /** Địa chỉ email. */
    private String email;

    /** Vai trò: BIDDER, SELLER, hoặc ADMIN. */
    private UserRole role;

    /**
     * Constructor mặc định.
     */
    protected User() {
        super();
    }

    /**
     * Constructor đầy đủ.
     *
     * @param username tên đăng nhập
     * @param password mật khẩu
     * @param email    email
     * @param role     vai trò
     */
    protected User(String username, String password, String email, UserRole role) {
        super();
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // ==================== Getters & Setters ====================

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
