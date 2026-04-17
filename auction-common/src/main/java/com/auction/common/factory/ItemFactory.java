package com.auction.common.factory;

import com.auction.common.entity.Art;
import com.auction.common.entity.Electronics;
import com.auction.common.entity.Item;
import com.auction.common.entity.Vehicle;
import com.auction.common.enums.ItemType;

/**
 * Factory Method Pattern — tạo Item theo loại.
 *
 * <p>Thay vì gọi {@code new Electronics()}, {@code new Art()}, {@code new Vehicle()}
 * ở nhiều nơi, ta gọi {@code ItemFactory.createItem(ItemType.ELECTRONICS)}.
 *
 * <p>Lợi ích: khi thêm loại sản phẩm mới, chỉ cần sửa Factory này,
 * không cần sửa code ở những nơi gọi tạo Item.
 */
public final class ItemFactory {

    /** Không cho phép tạo instance — chỉ dùng static method. */
    private ItemFactory() {
    }

    /**
     * Tạo một Item mới theo loại.
     *
     * <p>Ví dụ:
     * <pre>
     *   Item laptop = ItemFactory.createItem(ItemType.ELECTRONICS);
     *   laptop.setName("MacBook Pro M3");
     * </pre>
     *
     * @param type loại sản phẩm (ELECTRONICS, ART, VEHICLE)
     * @return instance của subclass tương ứng
     * @throws IllegalArgumentException nếu type không hợp lệ
     */
    public static Item createItem(ItemType type) {
        switch (type) {
            case ELECTRONICS:
                return new Electronics();
            case ART:
                return new Art();
            case VEHICLE:
                return new Vehicle();
            default:
                throw new IllegalArgumentException("Loại sản phẩm không hợp lệ: " + type);
        }
    }
}
