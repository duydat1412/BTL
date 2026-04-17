package com.auction.common.entity;

import com.auction.common.enums.ItemType;

/**
 * Lớp trừu tượng đại diện cho sản phẩm đấu giá.
 *
 * <p>Kế thừa từ {@link Entity}. Là lớp cha của {@link Electronics},
 * {@link Art}, {@link Vehicle}.
 *
 * <p>Abstraction: định nghĩa abstract method {@link #getDetailedDescription()}
 * buộc mỗi loại sản phẩm phải cung cấp mô tả chi tiết riêng.
 */
public abstract class Item extends Entity {

    private static final long serialVersionUID = 1L;

    /** Tên sản phẩm. */
    private String name;

    /** Mô tả chung. */
    private String description;

    /** Giá khởi điểm (VND). */
    private double startingPrice;

    /** ID của người bán (liên kết tới User). */
    private String sellerId;

    /** Loại sản phẩm: ELECTRONICS, ART, VEHICLE. */
    private ItemType itemType;

    /**
     * Constructor mặc định.
     */
    protected Item() {
        super();
    }

    /**
     * Constructor đầy đủ.
     *
     * @param name          tên sản phẩm
     * @param description   mô tả
     * @param startingPrice giá khởi điểm
     * @param sellerId      ID người bán
     * @param itemType      loại sản phẩm
     */
    protected Item(String name, String description, double startingPrice,
                   String sellerId, ItemType itemType) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.sellerId = sellerId;
        this.itemType = itemType;
    }

    /**
     * Trả về mô tả chi tiết riêng cho từng loại sản phẩm.
     * Mỗi subclass phải override — thể hiện tính Trừu tượng (Abstraction).
     *
     * @return mô tả chi tiết
     */
    public abstract String getDetailedDescription();

    // ==================== Getters & Setters ====================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }
}
