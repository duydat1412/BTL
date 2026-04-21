package com.auction.common.entity;

import com.auction.common.enums.ItemType;

/**
 * Sản phẩm đồ điện tử (laptop, điện thoại, tablet, ...).
 *
 * <p>Kế thừa từ {@link Item}, type tự động gán là {@link ItemType#ELECTRONICS}.
 * Thể hiện tính Đa hình qua override {@link #getInfo()} và
 * {@link #getDetailedDescription()}.
 */
public class Electronics extends Item {

    private static final long serialVersionUID = 1L;

    /** Thương hiệu (Apple, Samsung, ...). */
    private String brand;

    /** Model cụ thể (iPhone 15, Galaxy S24, ...). */
    private String model;

    /** Số tháng bảo hành còn lại. */
    private int warrantyMonths;

    /**
     * Constructor mặc định.
     */
    public Electronics() {
        super();
        setItemType(ItemType.ELECTRONICS);
    }

    /**
     * Constructor đầy đủ.
     *
     * @param name           tên sản phẩm
     * @param description    mô tả
     * @param startingPrice  giá khởi điểm
     * @param sellerId       ID người bán
     * @param brand          thương hiệu
     * @param model          model
     * @param warrantyMonths số tháng bảo hành
     */
    public Electronics(String name, String description, double startingPrice,
                       String sellerId, String brand, String model, int warrantyMonths) {
        super(name, description, startingPrice, sellerId, ItemType.ELECTRONICS);
        this.brand = brand;
        this.model = model;
        this.warrantyMonths = warrantyMonths;
    }

    // ==================== Polymorphism ====================

    @Override
    public String getInfo() {
        return String.format("Electronics[id=%s, name=%s, brand=%s, model=%s, price=%.0f]",
                getId(), getName(), brand, model, getStartingPrice());
    }

    @Override
    public String getDetailedDescription() {
        return String.format("Đồ điện tử: %s %s — %s. Bảo hành: %d tháng. Giá khởi điểm: %.0f VND.",
                brand, model, getDescription(), warrantyMonths, getStartingPrice());
    }

    // ==================== Getters & Setters ====================

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }
}
