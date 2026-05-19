package com.auction.common.entity;

import com.auction.common.enums.UserRole;

/**
 * Lop truu tuong dai dien cho nguoi dung trong he thong.
 *
 * <p>Ke thua tu {@link Entity}. La lop cha cua {@link Bidder},
 * {@link Seller}, {@link Admin}.
 */
public abstract class User extends Entity {

    private static final long serialVersionUID = 1L;

    /** Ten dang nhap - duy nhat trong he thong. */
    private String username;

    /** Mat khau (da hash bang BCrypt). */
    private String password;

    /** Dia chi email. */
    private String email;

    /** Vai tro: BIDDER, SELLER, hoac ADMIN. */
    private UserRole role;

    /** Ban status set by admin. */
    private boolean banned;

    /** Reason for ban, nullable when user is not banned. */
    private String banReason;

    /** Constructor mac dinh. */
    protected User() {
        super();
        this.banned = false;
        this.banReason = null;
    }

    /**
     * Constructor day du.
     *
     * @param username ten dang nhap
     * @param password mat khau
     * @param email email
     * @param role vai tro
     */
    protected User(String username, String password, String email, UserRole role) {
        super();
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.banned = false;
        this.banReason = null;
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

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public String getBanReason() {
        return banReason;
    }

    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }
}
