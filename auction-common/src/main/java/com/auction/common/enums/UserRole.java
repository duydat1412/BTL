package com.auction.common.enums;

/**
 * Vai trò của người dùng trong hệ thống đấu giá.
 *
 * <p>Mỗi User chỉ có đúng 1 role. Role quyết định quyền hạn:
 * BIDDER đặt giá, SELLER đăng sản phẩm, ADMIN quản lý hệ thống.
 */
public enum UserRole {

    /** Người đặt giá — tham gia đấu giá. */
    BIDDER("Người mua"),

    /** Người bán — đăng sản phẩm lên đấu giá. */
    SELLER("Người bán"),

    /** Quản trị viên — quản lý toàn hệ thống. */
    ADMIN("Quản trị viên");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Trả về tên hiển thị tiếng Việt.
     *
     * @return tên hiển thị
     */
    public String getDisplayName() {
        return displayName;
    }
}
