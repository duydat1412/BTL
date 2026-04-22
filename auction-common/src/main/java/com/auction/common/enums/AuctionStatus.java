package com.auction.common.enums;

/**
 * Trạng thái của một phiên đấu giá.
 *
 * <p>Vòng đời: OPEN → RUNNING → FINISHED → PAID hoặc CANCELED.
 * Dùng enum thay vì String để tránh gõ sai và IDE hỗ trợ gợi ý.
 */
public enum AuctionStatus {

    /** Phiên đấu giá mới tạo, chưa bắt đầu. */
    OPEN("Mở"),

    /** Đang diễn ra, cho phép đặt giá. */
    RUNNING("Đang diễn ra"),

    /** Đã kết thúc, chờ thanh toán. */
    FINISHED("Kết thúc"),

    /** Người thắng đã thanh toán. */
    PAID("Đã thanh toán"),

    /** Phiên bị hủy bỏ. */
    CANCELED("Đã hủy");

    private final String displayName;

    AuctionStatus(String displayName) {
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
