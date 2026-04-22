package com.auction.common.entity;

import com.auction.common.enums.ItemType;

/**
 * Phương tiện giao thông (xe máy, ô tô, ...).
 *
 * <p>Kế thừa từ {@link Item}, type tự động gán là {@link ItemType#VEHICLE}.
 * Thể hiện tính Đa hình qua override {@link #getInfo()} và
 * {@link #getDetailedDescription()}.
 */
public class Vehicle extends Item {

    private static final long serialVersionUID = 1L;

    /** Hãng sản xuất (Toyota, Honda, ...). */
    private String manufacturer;

    /** Năm sản xuất. */
    private int yearOfManufacture;

    /** Số km đã đi. */
    private int mileage;

    /**
     * Constructor mặc định.
     */
    public Vehicle() {
        super();
        setItemType(ItemType.VEHICLE);
    }

    /**
     * Constructor đầy đủ.
     *
     * @param name              tên phương tiện
     * @param description       mô tả
     * @param startingPrice     giá khởi điểm
     * @param sellerId          ID người bán
     * @param manufacturer      hãng sản xuất
     * @param yearOfManufacture năm sản xuất
     * @param mileage           số km đã đi
     */
    public Vehicle(String name, String description, double startingPrice,
                   String sellerId, String manufacturer, int yearOfManufacture, int mileage) {
        super(name, description, startingPrice, sellerId, ItemType.VEHICLE);
        this.manufacturer = manufacturer;
        this.yearOfManufacture = yearOfManufacture;
        this.mileage = mileage;
    }

    // ==================== Polymorphism ====================

    @Override
    public String getInfo() {
        return String.format("Vehicle[id=%s, name=%s, manufacturer=%s, year=%d, mileage=%dkm, price=%.0f]",
                getId(), getName(), manufacturer, yearOfManufacture, mileage, getStartingPrice());
    }

    @Override
    public String getDetailedDescription() {
        return String.format("Phương tiện: %s %s (%d). Số km: %d km. Giá khởi điểm: %.0f VND.",
                manufacturer, getName(), yearOfManufacture, mileage, getStartingPrice());
    }

    // ==================== Getters & Setters ====================

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getYearOfManufacture() {
        return yearOfManufacture;
    }

    public void setYearOfManufacture(int yearOfManufacture) {
        this.yearOfManufacture = yearOfManufacture;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }
}
