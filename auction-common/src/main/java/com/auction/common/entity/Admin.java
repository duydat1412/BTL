package com.auction.common.entity;

import com.auction.common.enums.UserRole;

/**
 * Quản trị viên — quản lý toàn bộ hệ thống.
 *
 * <p>Kế thừa từ {@link User}, role tự động gán là {@link UserRole#ADMIN}.
 * Thể hiện tính Đa hình (Polymorphism) qua override {@link #getInfo()}.
 */
public class Admin extends User {

    private static final long serialVersionUID = 1L;

    /** Phòng ban quản lý. */
    private String department;

    /**
     * Constructor mặc định.
     */
    public Admin() {
        super();
        setRole(UserRole.ADMIN);
    }

    /**
     * Constructor đầy đủ.
     *
     * @param username   tên đăng nhập
     * @param password   mật khẩu
     * @param email      email
     * @param department phòng ban
     */
    public Admin(String username, String password, String email, String department) {
        super(username, password, email, UserRole.ADMIN);
        this.department = department;
    }

    // ==================== Polymorphism ====================

    @Override
    public String getInfo() {
        return String.format("Admin[id=%s, username=%s, email=%s, department=%s]",
                getId(), getUsername(), getEmail(), department);
    }

    // ==================== Getters & Setters ====================

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
