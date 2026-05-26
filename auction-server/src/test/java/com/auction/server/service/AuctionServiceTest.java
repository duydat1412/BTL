package com.auction.server.service;

import com.auction.common.entity.*;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.ItemType;
import com.auction.common.message.*;
import com.auction.server.datastore.DataStore;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuctionServiceTest {

    private String adminId;
    private String sellerId;
    private String itemId;

    @BeforeEach
    void setUp() {
        DataStore.getInstance().getUsers().clear();
        DataStore.getInstance().getItems().clear();
        DataStore.getInstance().getAuctions().clear();
        DataStore.getInstance().getBidTransactions().clear();

        // Create admin
        Admin admin = new Admin("admin", "hash", "admin@test.com", "IT");
        admin.setId("admin-1");
        DataStore.getInstance().getUsers().add(admin);
        adminId = admin.getId();

        // Create seller
        Seller seller = new Seller("seller", "hash", "seller@test.com");
        seller.setId("seller-1");
        DataStore.getInstance().getUsers().add(seller);
        sellerId = seller.getId();

        // Create item
        Electronics item = new Electronics();
        item.setName("Test Phone");
        item.setDescription("A phone");
        item.setStartingPrice(1000);
        item.setSellerId(sellerId);
        item.setItemType(ItemType.ELECTRONICS);
        item.setBrand("Test");
        item.setModel("X1");
        DataStore.getInstance().getItems().add(item);
        itemId = item.getId();
    }

    @AfterEach
    void tearDown() {
        DataStore.getInstance().getUsers().clear();
        DataStore.getInstance().getItems().clear();
        DataStore.getInstance().getAuctions().clear();
        DataStore.getInstance().getBidTransactions().clear();
    }

    @Nested
    @DisplayName("createAuction tests")
    class CreateAuctionTests {

        @Test
        @DisplayName("Create auction success")
        void createAuction_success() {
            CreateAuctionRequest req = new CreateAuctionRequest(itemId, null, null);
            ClientResponse res = AuctionService.createAuction(req);
            assertTrue(res.isSuccess());
            assertNotNull(res.getData());
            assertInstanceOf(Auction.class, res.getData());
            Auction auction = (Auction) res.getData();
            assertEquals(AuctionStatus.OPEN, auction.getStatus());
            assertEquals(itemId, auction.getItemId());
            assertEquals(sellerId, auction.getSellerId());
        }

        @Test
        @DisplayName("Create auction with null itemId fails")
        void createAuction_nullItemId_fails() {
            CreateAuctionRequest req = new CreateAuctionRequest(null, null, null);
            ClientResponse res = AuctionService.createAuction(req);
            assertFalse(res.isSuccess());
        }

        @Test
        @DisplayName("Create auction with non-existent item fails")
        void createAuction_invalidItem_fails() {
            CreateAuctionRequest req = new CreateAuctionRequest("non-existent", null, null);
            ClientResponse res = AuctionService.createAuction(req);
            assertFalse(res.isSuccess());
        }

        @Test
        @DisplayName("Cannot create duplicate auction for same item")
        void createAuction_duplicate_fails() {
            CreateAuctionRequest req1 = new CreateAuctionRequest(itemId, null, null);
            assertTrue(AuctionService.createAuction(req1).isSuccess());
            CreateAuctionRequest req2 = new CreateAuctionRequest(itemId, null, null);
            assertFalse(AuctionService.createAuction(req2).isSuccess());
        }

        @Test
        @DisplayName("End time must be after start time")
        void createAuction_endBeforeStart_fails() {
            java.time.LocalDateTime start = java.time.LocalDateTime.now();
            java.time.LocalDateTime end = start.minusHours(1);
            CreateAuctionRequest req = new CreateAuctionRequest(itemId, start, end);
            ClientResponse res = AuctionService.createAuction(req);
            assertFalse(res.isSuccess());
        }
    }

    @Nested
    @DisplayName("getAuctions tests")
    class GetAuctionsTests {

        private String auctionId;

        @BeforeEach
        void createAuction() {
            CreateAuctionRequest req = new CreateAuctionRequest(itemId, null, null);
            ClientResponse res = AuctionService.createAuction(req);
            auctionId = ((Auction) res.getData()).getId();
        }

        @Test
        @DisplayName("Get all auctions")
        void getAuctions_all_success() {
            ClientResponse res = AuctionService.getAuctions(null);
            assertTrue(res.isSuccess());
            assertInstanceOf(List.class, res.getData());
            List<?> auctions = (List<?>) res.getData();
            assertEquals(1, auctions.size());
        }

        @Test
        @DisplayName("Get auctions filtered by sellerId")
        void getAuctions_filterBySeller_success() {
            GetAuctionsRequest req = new GetAuctionsRequest(sellerId, null);
            ClientResponse res = AuctionService.getAuctions(req);
            List<?> auctions = (List<?>) res.getData();
            assertEquals(1, auctions.size());
        }

        @Test
        @DisplayName("Get auctions filtered by status")
        void getAuctions_filterByStatus_success() {
            GetAuctionsRequest req = new GetAuctionsRequest(null, AuctionStatus.OPEN.name());
            ClientResponse res = AuctionService.getAuctions(req);
            List<?> auctions = (List<?>) res.getData();
            assertEquals(1, auctions.size());
        }

        @Test
        @DisplayName("Get auctions returns empty for unknown seller")
        void getAuctions_noMatch() {
            GetAuctionsRequest req = new GetAuctionsRequest("unknown-seller", null);
            ClientResponse res = AuctionService.getAuctions(req);
            List<?> auctions = (List<?>) res.getData();
            assertTrue(auctions.isEmpty());
        }
    }

    @Nested
    @DisplayName("getAuction tests")
    class GetAuctionTests {

        private String auctionId;

        @BeforeEach
        void createAuction() {
            CreateAuctionRequest req = new CreateAuctionRequest(itemId, null, null);
            auctionId = ((Auction) AuctionService.createAuction(req).getData()).getId();
        }

        @Test
        @DisplayName("Get auction by ID success")
        void getAuction_success() {
            ClientResponse res = AuctionService.getAuction(auctionId);
            assertTrue(res.isSuccess());
            assertInstanceOf(Auction.class, res.getData());
        }

        @Test
        @DisplayName("Get auction with invalid ID fails")
        void getAuction_invalidId_fails() {
            ClientResponse res = AuctionService.getAuction("bad-id");
            assertFalse(res.isSuccess());
        }
    }

    @Nested
    @DisplayName("cancelAuction tests")
    class CancelAuctionTests {

        private String auctionId;

        @BeforeEach
        void createAuction() {
            CreateAuctionRequest req = new CreateAuctionRequest(itemId, null, null);
            auctionId = ((Auction) AuctionService.createAuction(req).getData()).getId();
        }

        @Test
        @DisplayName("Admin can cancel OPEN auction")
        void cancelAuction_byAdmin_success() {
            CancelAuctionRequest req = new CancelAuctionRequest(adminId, auctionId, "Test cancellation");
            ClientResponse res = AuctionService.cancelAuction(req);
            assertTrue(res.isSuccess());
            Auction cancelled = (Auction) res.getData();
            assertEquals(AuctionStatus.CANCELED, cancelled.getStatus());
        }

        @Test
        @DisplayName("Non-admin cannot cancel auction")
        void cancelAuction_byNonAdmin_fails() {
            CancelAuctionRequest req = new CancelAuctionRequest(sellerId, auctionId, "No perm");
            ClientResponse res = AuctionService.cancelAuction(req);
            assertFalse(res.isSuccess());
        }

        @Test
        @DisplayName("Cancel non-existent auction fails")
        void cancelAuction_invalidAuction_fails() {
            CancelAuctionRequest req = new CancelAuctionRequest(adminId, "bad-id", "test");
            ClientResponse res = AuctionService.cancelAuction(req);
            assertFalse(res.isSuccess());
        }

        @Test
        @DisplayName("Cannot cancel finished auction")
        void cancelAuction_alreadyFinished_fails() {
            Auction auction = DataStore.getInstance().getAuctions().stream()
                    .filter(a -> a.getId().equals(auctionId)).findFirst().orElseThrow();
            auction.setStatus(AuctionStatus.FINISHED);
            CancelAuctionRequest req = new CancelAuctionRequest(adminId, auctionId, "Late");
            ClientResponse res = AuctionService.cancelAuction(req);
            assertFalse(res.isSuccess());
        }
    }
}
