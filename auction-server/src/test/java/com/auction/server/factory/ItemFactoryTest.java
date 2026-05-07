package com.auction.server.factory;

import com.auction.common.entity.Art;
import com.auction.common.entity.Electronics;
import com.auction.common.entity.Item;
import com.auction.common.entity.Vehicle;
import com.auction.common.enums.ItemType;
import com.auction.common.factory.ItemFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho ItemFactory — kiểm tra Factory Method Pattern.
 */
class ItemFactoryTest {

    @Test
    @DisplayName("createItem(ELECTRONICS) → Electronics instance")
    void createItem_electronics_returnsElectronicsInstance() {
        Item item = ItemFactory.createItem(ItemType.ELECTRONICS);
        assertNotNull(item);
        assertTrue(item instanceof Electronics);
        assertEquals(ItemType.ELECTRONICS, item.getItemType());
    }

    @Test
    @DisplayName("createItem(ART) → Art instance")
    void createItem_art_returnsArtInstance() {
        Item item = ItemFactory.createItem(ItemType.ART);
        assertTrue(item instanceof Art);
        assertEquals(ItemType.ART, item.getItemType());
    }

    @Test
    @DisplayName("createItem(VEHICLE) → Vehicle instance")
    void createItem_vehicle_returnsVehicleInstance() {
        Item item = ItemFactory.createItem(ItemType.VEHICLE);
        assertTrue(item instanceof Vehicle);
        assertEquals(ItemType.VEHICLE, item.getItemType());
    }

    @Test
    @DisplayName("createItem(null) → throws exception")
    void createItem_nullType_throwsException() {
        assertThrows(NullPointerException.class,
                () -> ItemFactory.createItem(null));
    }

    @Test
    @DisplayName("Mỗi lần gọi → instance mới (khác ID)")
    void createItem_calledTwice_returnsDifferentInstances() {
        Item item1 = ItemFactory.createItem(ItemType.ELECTRONICS);
        Item item2 = ItemFactory.createItem(ItemType.ELECTRONICS);
        assertNotSame(item1, item2);
        assertNotEquals(item1.getId(), item2.getId());
    }

    @Test
    @DisplayName("Polymorphism: tất cả type có getInfo() + getDetailedDescription()")
    void createItem_allTypes_supportPolymorphism() {
        for (ItemType type : ItemType.values()) {
            Item item = ItemFactory.createItem(type);
            assertNotNull(item.getInfo());
            assertNotNull(item.getDetailedDescription());
        }
    }
}
