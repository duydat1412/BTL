package com.auction.common.enums;

/**
 * Loại sản phẩm trong hệ thống đấu giá.
 *
 * <p>Dùng trong ItemFactory (Factory Method) để tạo đúng subclass:
 * ELECTRONICS → Electronics, ART → Art, VEHICLE → Vehicle.
 */
public enum ItemType {

    /** Đồ điện tử (laptop, điện thoại, ...). */
    ELECTRONICS("Đồ điện tử"),

    /** Tác phẩm nghệ thuật (tranh, tượng, ...). */
    ART("Nghệ thuật"),

    /** Phương tiện giao thông (xe máy, ô tô, ...). */
    VEHICLE("Phương tiện");

    private final String displayName;

    ItemType(String displayName) {
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
