package com.auction.server.service;

import com.auction.common.entity.*;
import com.auction.common.enums.ItemType;
import com.auction.common.message.*;
import com.auction.server.datastore.DataStore;
import com.auction.server.repository.SerializableItemRepository;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {

    private SerializableItemRepository itemRepo;

    @BeforeEach
    void setUp() {
        DataStore.getInstance().getItems().clear();
        DataStore.getInstance().getAuctions().clear();
        itemRepo = new SerializableItemRepository();
    }

    @AfterEach
    void tearDown() {
        DataStore.getInstance().getItems().clear();
        DataStore.getInstance().getAuctions().clear();
    }

    private String createTestItem(String name, double price, String sellerId, ItemType type) {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("durationMinutes", "60");
        if (type == ItemType.ELECTRONICS) {
            attrs.put("brand", "TestBrand");
            attrs.put("model", "TestModel");
            attrs.put("warrantyMonths", "12");
        } else if (type == ItemType.ART) {
            attrs.put("artist", "TestArtist");
            attrs.put("medium", "Oil");
            attrs.put("year", "2024");
        } else if (type == ItemType.VEHICLE) {
            attrs.put("manufacturer", "TestMfg");
            attrs.put("yearOfManufacture", "2023");
            attrs.put("mileage", "10000");
        }
        CreateItemRequest req = new CreateItemRequest(name, "Description of " + name, price, sellerId, type, attrs);
        ClientResponse res = ItemService.C(req);
        assertTrue(res.isSuccess());
        return ((Item) res.getData()).getId();
    }

    @Nested
    @DisplayName("CREATE tests")
    class CreateTests {

        @Test
        @DisplayName("Create Electronics success")
        void create_electronics_success() {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("brand", "Samsung");
            attrs.put("model", "Galaxy");
            attrs.put("warrantyMonths", "24");
            attrs.put("durationMinutes", "60");

            CreateItemRequest req = new CreateItemRequest("Smartphone", "Latest model", 15000, "seller-1", ItemType.ELECTRONICS, attrs);
            ClientResponse res = ItemService.C(req);
            assertTrue(res.isSuccess());
            assertNotNull(res.getData());
            assertInstanceOf(Electronics.class, res.getData());
            Electronics item = (Electronics) res.getData();
            assertEquals("Smartphone", item.getName());
            assertEquals("Samsung", item.getBrand());
        }

        @Test
        @DisplayName("Create Art success")
        void create_art_success() {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("artist", "Picasso");
            attrs.put("medium", "Oil");
            attrs.put("year", "1937");
            attrs.put("durationMinutes", "120");

            CreateItemRequest req = new CreateItemRequest("Guernica", "Famous painting", 500000, "seller-2", ItemType.ART, attrs);
            ClientResponse res = ItemService.C(req);
            assertTrue(res.isSuccess());
            Art item = (Art) res.getData();
            assertEquals("Picasso", item.getArtist());
        }

        @Test
        @DisplayName("Create Vehicle success")
        void create_vehicle_success() {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("manufacturer", "Toyota");
            attrs.put("yearOfManufacture", "2022");
            attrs.put("mileage", "5000");
            attrs.put("durationMinutes", "30");

            CreateItemRequest req = new CreateItemRequest("Camry", "Reliable car", 800000, "seller-3", ItemType.VEHICLE, attrs);
            ClientResponse res = ItemService.C(req);
            assertTrue(res.isSuccess());
            Vehicle item = (Vehicle) res.getData();
            assertEquals("Toyota", item.getManufacturer());
        }

        @Test
        @DisplayName("Create with empty name fails")
        void create_emptyName_fails() {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("durationMinutes", "60");
            CreateItemRequest req = new CreateItemRequest("", "desc", 1000, "seller-1", ItemType.ELECTRONICS, attrs);
            ClientResponse res = ItemService.C(req);
            assertFalse(res.isSuccess());
        }

        @Test
        @DisplayName("Create with zero price fails")
        void create_zeroPrice_fails() {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("durationMinutes", "60");
            CreateItemRequest req = new CreateItemRequest("Item", "desc", 0, "seller-1", ItemType.ELECTRONICS, attrs);
            ClientResponse res = ItemService.C(req);
            assertFalse(res.isSuccess());
        }

        @Test
        @DisplayName("Create with null sellerId fails")
        void create_nullSeller_fails() {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("durationMinutes", "60");
            CreateItemRequest req = new CreateItemRequest("Item", "desc", 1000, null, ItemType.ELECTRONICS, attrs);
            ClientResponse res = ItemService.C(req);
            assertFalse(res.isSuccess());
        }

        @Test
        @DisplayName("Create with null item type fails")
        void create_nullType_fails() {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("durationMinutes", "60");
            CreateItemRequest req = new CreateItemRequest("Item", "desc", 1000, "seller-1", null, attrs);
            ClientResponse res = ItemService.C(req);
            assertFalse(res.isSuccess());
        }

        @Test
        @DisplayName("Create with invalid duration falls back safely")
        void create_invalidDuration_stillSucceeds() {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("durationMinutes", "not-a-number");
            CreateItemRequest req = new CreateItemRequest("AuctionItem", "desc", 2000, "seller-1", ItemType.ELECTRONICS, attrs);
            ClientResponse res = ItemService.C(req);
            assertTrue(res.isSuccess());
            assertEquals(1, DataStore.getInstance().getAuctions().size());
        }

        @Test
        @DisplayName("Create also creates auction")
        void create_createsAuction() {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("durationMinutes", "60");
            CreateItemRequest req = new CreateItemRequest("AuctionItem", "desc", 2000, "seller-1", ItemType.ELECTRONICS, attrs);
            ClientResponse res = ItemService.C(req);
            assertTrue(res.isSuccess());
            assertEquals(1, DataStore.getInstance().getAuctions().size());
        }
    }

    @Nested
    @DisplayName("READ tests")
    class ReadTests {

        private String itemId1;
        private String itemId2;

        @BeforeEach
        void createItems() {
            itemId1 = createTestItem("Item A", 1000, "seller-1", ItemType.ELECTRONICS);
            itemId2 = createTestItem("Item B", 2000, "seller-2", ItemType.ART);
        }

        @Test
        @DisplayName("GetItems returns all items")
        void read_all_success() {
            GetItemsRequest req = new GetItemsRequest(null, null);
            ClientResponse res = ItemService.R(req);
            assertTrue(res.isSuccess());
            assertInstanceOf(java.util.List.class, res.getData());
            java.util.List<?> items = (java.util.List<?>) res.getData();
            assertEquals(2, items.size());
        }

        @Test
        @DisplayName("GetItems filtered by sellerId")
        void read_filterBySeller_success() {
            GetItemsRequest req = new GetItemsRequest("seller-1", null);
            ClientResponse res = ItemService.R(req);
            java.util.List<?> items = (java.util.List<?>) res.getData();
            assertEquals(1, items.size());
        }

        @Test
        @DisplayName("GetItems filtered by type")
        void read_filterByType_success() {
            GetItemsRequest req = new GetItemsRequest(null, ItemType.ART);
            ClientResponse res = ItemService.R(req);
            java.util.List<?> items = (java.util.List<?>) res.getData();
            assertEquals(1, items.size());
        }

        @Test
        @DisplayName("empty result when no match")
        void read_filterNoMatch_success() {
            GetItemsRequest req = new GetItemsRequest("seller-none", null);
            ClientResponse res = ItemService.R(req);
            java.util.List<?> items = (java.util.List<?>) res.getData();
            assertTrue(items.isEmpty());
        }
    }

    @Nested
    @DisplayName("UPDATE tests")
    class UpdateTests {

        private String itemId;
        private ItemService itemService;

        @BeforeEach
        void createItem() {
            itemService = new ItemService();
            itemId = createTestItem("Original", 1000, "seller-1", ItemType.ELECTRONICS);
        }

        @Test
        @DisplayName("Update item name success")
        void update_name_success() {
            Map<String, String> attrs = new HashMap<>();
            UpdateItemRequest req = new UpdateItemRequest(itemId, "Updated Name", null, null, null, attrs);
            ClientResponse res = itemService.U(req);
            assertTrue(res.isSuccess());
            Item updated = itemRepo.findById(itemId);
            assertEquals("Updated Name", updated.getName());
        }

        @Test
        @DisplayName("Update item price success")
        void update_price_success() {
            Map<String, String> attrs = new HashMap<>();
            UpdateItemRequest req = new UpdateItemRequest(itemId, null, null, 5000.0, null, attrs);
            ClientResponse res = itemService.U(req);
            assertTrue(res.isSuccess());
            Item updated = itemRepo.findById(itemId);
            assertEquals(5000.0, updated.getStartingPrice());
        }

        @Test
        @DisplayName("Update Electronics attributes success")
        void update_electronicsAttrs_success() {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("brand", "Apple");
            attrs.put("model", "iPhone");
            UpdateItemRequest req = new UpdateItemRequest(itemId, null, null, null, null, attrs);
            ClientResponse res = itemService.U(req);
            assertTrue(res.isSuccess());
            Electronics updated = (Electronics) itemRepo.findById(itemId);
            assertEquals("Apple", updated.getBrand());
            assertEquals("iPhone", updated.getModel());
        }

        @Test
        @DisplayName("Update non-existent item fails")
        void update_invalidId_fails() {
            Map<String, String> attrs = new HashMap<>();
            UpdateItemRequest req = new UpdateItemRequest("bad-id", "Name", null, null, null, attrs);
            ClientResponse res = itemService.U(req);
            assertFalse(res.isSuccess());
        }

        @Test
        @DisplayName("Update vehicle attributes success")
        void update_vehicleAttrs_success() {
            String vehicleItemId = createTestItem("Car", 3000, "seller-1", ItemType.VEHICLE);
            Map<String, String> attrs = new HashMap<>();
            attrs.put("manufacturer", "Honda");
            attrs.put("yearOfManufacture", "2024");
            attrs.put("mileage", "200");
            UpdateItemRequest req = new UpdateItemRequest(vehicleItemId, null, null, null, null, attrs);
            ClientResponse res = itemService.U(req);
            assertTrue(res.isSuccess());
            Vehicle updated = (Vehicle) itemRepo.findById(vehicleItemId);
            assertEquals("Honda", updated.getManufacturer());
            assertEquals(2024, updated.getYearOfManufacture());
            assertEquals(200, updated.getMileage());
        }
    }

    @Nested
    @DisplayName("DELETE tests")
    class DeleteTests {

        private String itemId;
        private ItemService itemService;

        @BeforeEach
        void createItem() {
            itemService = new ItemService();
            itemId = createTestItem("ToDelete", 1000, "seller-1", ItemType.ELECTRONICS);
        }

        @Test
        @DisplayName("Delete by owner success")
        void delete_byOwner_success() {
            DeleteItemRequest req = new DeleteItemRequest(itemId, "seller-1");
            ClientResponse res = itemService.D(req, "seller-1");
            assertTrue(res.isSuccess());
            assertNull(itemRepo.findById(itemId));
        }

        @Test
        @DisplayName("Delete by non-owner fails")
        void delete_byNonOwner_fails() {
            DeleteItemRequest req = new DeleteItemRequest(itemId, "seller-1");
            ClientResponse res = itemService.D(req, "other-user");
            assertFalse(res.isSuccess());
        }

        @Test
        @DisplayName("Delete finished auction item fails")
        void delete_finishedAuctionItem_fails() {
            Auction auction = DataStore.getInstance().getAuctions().stream()
                    .filter(a -> itemId.equals(a.getItemId()))
                    .findFirst()
                    .orElseThrow();
            auction.setStatus(com.auction.common.enums.AuctionStatus.FINISHED);

            DeleteItemRequest req = new DeleteItemRequest(itemId, "seller-1");
            ClientResponse res = itemService.D(req, "seller-1");
            assertFalse(res.isSuccess());
            assertNotNull(itemRepo.findById(itemId));
        }
    }
}
