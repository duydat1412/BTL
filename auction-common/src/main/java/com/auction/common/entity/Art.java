package com.auction.common.entity;

import com.auction.common.enums.ItemType;

/**
 * Tác phẩm nghệ thuật (tranh, tượng, ...).
 *
 * <p>Kế thừa từ {@link Item}, type tự động gán là {@link ItemType#ART}.
 * Thể hiện tính Đa hình qua override {@link #getInfo()} và
 * {@link #getDetailedDescription()}.
 */
public class Art extends Item {

    private static final long serialVersionUID = 1L;

    /** Tên nghệ sĩ / họa sĩ. */
    private String artist;

    /** Năm sáng tác. */
    private int year;

    /** Chất liệu (sơn dầu, watercolor, acrylic, ...). */
    private String medium;

    /**
     * Constructor mặc định.
     */
    public Art() {
        super();
        setItemType(ItemType.ART);
    }

    /**
     * Constructor đầy đủ.
     *
     * @param name          tên tác phẩm
     * @param description   mô tả
     * @param startingPrice giá khởi điểm
     * @param sellerId      ID người bán
     * @param artist        tên nghệ sĩ
     * @param year          năm sáng tác
     * @param medium        chất liệu
     */
    public Art(String name, String description, double startingPrice,
               String sellerId, String artist, int year, String medium) {
        super(name, description, startingPrice, sellerId, ItemType.ART);
        this.artist = artist;
        this.year = year;
        this.medium = medium;
    }

    // ==================== Polymorphism ====================

    @Override
    public String getInfo() {
        return String.format("Art[id=%s, name=%s, artist=%s, year=%d, medium=%s, price=%.0f]",
                getId(), getName(), artist, year, medium, getStartingPrice());
    }

    @Override
    public String getDetailedDescription() {
        return String.format("Tác phẩm nghệ thuật: \"%s\" của %s (%d). Chất liệu: %s. Giá khởi điểm: %.0f VND.",
                getName(), artist, year, medium, getStartingPrice());
    }

    // ==================== Getters & Setters ====================

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }
}
