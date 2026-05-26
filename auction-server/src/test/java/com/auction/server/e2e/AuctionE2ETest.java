package com.auction.server.e2e;

import com.auction.common.entity.*;
import com.auction.common.enums.*;
import com.auction.common.message.*;
import com.auction.server.datastore.DataStore;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuctionE2ETest {

    private E2ETestHelper.TestServer server;
    private int port;

    @BeforeEach
    void setUp() throws IOException {
        E2ETestHelper.resetDataStore();
        server = E2ETestHelper.startServer();
        port = server.getPort();
    }

    @AfterEach
    void tearDown() {
        server.close();
        E2ETestHelper.resetDataStore();
    }

    // ==================== Helper ====================

    private record ClientStreams(Socket socket, ObjectOutputStream out, ObjectInputStream in) implements AutoCloseable {
        static ClientStreams connect(int port) throws IOException {
            Socket socket = E2ETestHelper.connectToServer(port);
            ObjectOutputStream out = E2ETestHelper.createOutStream(socket);
            ObjectInputStream in = E2ETestHelper.createInStream(socket);
            return new ClientStreams(socket, out, in);
        }
        ClientResponse sendReceive(ClientRequest req) throws Exception {
            return E2ETestHelper.sendReceive(out, in, req);
        }
        @Override
        public void close() throws Exception {
            socket.close();
        }
    }

    private ClientStreams connect() throws IOException {
        return ClientStreams.connect(port);
    }

    private ClientResponse register(ClientStreams client, String user, String pass, String email, UserRole role) throws Exception {
        return client.sendReceive(new ClientRequest(Action.REGISTER, new RegisterRequest(user, pass, email, role)));
    }

    private ClientResponse login(ClientStreams client, String user, String pass) throws Exception {
        return client.sendReceive(new ClientRequest(Action.LOGIN, new LoginRequest(user, pass)));
    }

    private String loginAndGetId(ClientStreams client, String user, String pass) throws Exception {
        ClientResponse res = login(client, user, pass);
        assertTrue(res.isSuccess());
        return ((AuthUserData) res.getData()).getUserId();
    }

    private String registerAndLogin(ClientStreams client, String user, String pass, String email, UserRole role) throws Exception {
        register(client, user, pass, email, role);
        ClientResponse res = login(client, user, pass);
        assertTrue(res.isSuccess(), "Login after register should succeed");
        return ((AuthUserData) res.getData()).getUserId();
    }

    private Map<String, String> makeAttrs(String... kv) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put(kv[i], kv[i + 1]);
        return m;
    }

    // ==================== Tests ====================

    @Test
    @Order(1)
    @DisplayName("E2E: Register BIDDER -> Login -> GetAuctions (empty)")
    void registerBidder_login_getAuctions() throws Exception {
        try (var client = connect()) {
            ClientResponse reg = register(client, "bidder1", "Pass@123", "b@test.com", UserRole.BIDDER);
            assertTrue(reg.isSuccess());

            String userId = loginAndGetId(client, "bidder1", "Pass@123");
            assertNotNull(userId);

            ClientResponse auctions = client.sendReceive(new ClientRequest(Action.GET_AUCTIONS, null));
            assertTrue(auctions.isSuccess());
            assertNotNull(auctions.getData());
            List<?> list = (List<?>) auctions.getData();
            assertTrue(list.isEmpty(), "No auctions yet");
        }
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Register SELLER -> Create Item -> Get Items")
    void sellerCreateItem_getItems() throws Exception {
        try (var client = connect()) {
            String sellerId = registerAndLogin(client, "seller1", "Pass@123", "s@test.com", UserRole.SELLER);

            Map<String, String> attrs = makeAttrs("brand", "Apple", "model", "iPhone15", "warrantyMonths", "12", "durationMinutes", "60");
            ClientResponse createItem = client.sendReceive(
                    new ClientRequest(Action.CREATE_ITEM, new CreateItemRequest("iPhone", "Latest iPhone", 20000, sellerId, ItemType.ELECTRONICS, attrs)));
            assertTrue(createItem.isSuccess());
            assertNotNull(createItem.getData());
            assertInstanceOf(Electronics.class, createItem.getData());
            assertEquals("Apple", ((Electronics) createItem.getData()).getBrand());

            ClientResponse getItems = client.sendReceive(
                    new ClientRequest(Action.GET_ITEMS, new GetItemsRequest(sellerId, null)));
            assertTrue(getItems.isSuccess());
            List<?> items = (List<?>) getItems.getData();
            assertEquals(1, items.size());
        }
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Create item creates auction automatically")
    void createItem_createsAuction() throws Exception {
        try (var client = connect()) {
            String sellerId = registerAndLogin(client, "seller2", "Pass@123", "s2@test.com", UserRole.SELLER);

            Map<String, String> attrs = makeAttrs("artist", "Picasso", "medium", "Oil", "year", "1937", "durationMinutes", "60");
            client.sendReceive(new ClientRequest(Action.CREATE_ITEM,
                    new CreateItemRequest("Guernica", "Masterpiece", 500000, sellerId, ItemType.ART, attrs)));

            ClientResponse auctions = client.sendReceive(new ClientRequest(Action.GET_AUCTIONS, null));
            assertTrue(auctions.isSuccess());
            List<?> list = (List<?>) auctions.getData();
            assertEquals(1, list.size());
            Auction auction = (Auction) list.get(0);
            assertEquals(AuctionStatus.RUNNING, auction.getStatus());
        }
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Register BIDDER + SELLER -> Create Item -> Auction -> Bid")
    void fullBidFlow() throws Exception {
        try (var seller = connect(); var bidder = connect()) {
            String sellerId = registerAndLogin(seller, "seller3", "Pass@123", "s3@test.com", UserRole.SELLER);
            String bidderId = registerAndLogin(bidder, "bidder3", "Pass@123", "b3@test.com", UserRole.BIDDER);

            Map<String, String> attrs = makeAttrs("manufacturer", "Toyota", "yearOfManufacture", "2022", "mileage", "5000", "durationMinutes", "60");
            seller.sendReceive(new ClientRequest(Action.CREATE_ITEM,
                    new CreateItemRequest("Camry", "Reliable car", 10000, sellerId, ItemType.VEHICLE, attrs)));

            List<Auction> auctions = (List<Auction>) seller.sendReceive(new ClientRequest(Action.GET_AUCTIONS, null)).getData();
            assertEquals(1, auctions.size());
            String auctionId = auctions.get(0).getId();

            Auction auction = DataStore.getInstance().getAuctions().stream()
                    .filter(a -> a.getId().equals(auctionId)).findFirst().orElseThrow();
            auction.setStatus(AuctionStatus.RUNNING);

            ClientResponse bid = bidder.sendReceive(
                    new ClientRequest(Action.PLACE_BID, new PlaceBidRequest(auctionId, bidderId, 15000, false)));
            assertTrue(bid.isSuccess(), "Bid should succeed: " + bid.getMessage());

            ClientResponse history = bidder.sendReceive(
                    new ClientRequest(Action.GET_BID_HISTORY, new GetBidHistoryRequest(auctionId)));
            assertTrue(history.isSuccess());
            List<?> bidHistory = (List<?>) history.getData();
            assertEquals(1, bidHistory.size(), "Should have 1 bid in history");

            Auction updatedAuction = DataStore.getInstance().getAuctions().stream()
                    .filter(a -> a.getId().equals(auctionId)).findFirst().orElseThrow();
            assertEquals(15000, updatedAuction.getCurrentPrice());
            assertEquals(bidderId, updatedAuction.getHighestBidderId());
        }
    }

    @Test
    @Order(5)
    @DisplayName("E2E: Real-time push — two clients, bid triggers ServerPushMessage on second client")
    void realtimePush() throws Exception {
        try (var clientA = connect(); var clientB = connect()) {
            String sellerId = registerAndLogin(clientA, "seller", "Pass@123", "s@t.com", UserRole.SELLER);
            String bidderId = registerAndLogin(clientB, "bidder", "Pass@123", "b@t.com", UserRole.BIDDER);

            Map<String, String> attrs = makeAttrs("brand", "Test", "model", "X", "warrantyMonths", "12", "durationMinutes", "60");
            clientA.sendReceive(new ClientRequest(Action.CREATE_ITEM,
                    new CreateItemRequest("TestItem", "desc", 5000, sellerId, ItemType.ELECTRONICS, attrs)));

            String auctionId = DataStore.getInstance().getAuctions().stream().findFirst().orElseThrow().getId();
            DataStore.getInstance().getAuctions().stream()
                    .filter(a -> a.getId().equals(auctionId)).findFirst().orElseThrow()
                    .setStatus(AuctionStatus.RUNNING);

            clientA.sendReceive(new ClientRequest(Action.PLACE_BID,
                    new PlaceBidRequest(auctionId, sellerId, 6000, false)));

            ServerPushMessage push;
            do {
                push = E2ETestHelper.readPush(clientB.in());
            } while (push.getType() != ServerPushMessage.PushType.NEW_BID);
            assertEquals(ServerPushMessage.PushType.NEW_BID, push.getType());
        }
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Register auto-bid and verify trigger on counter-bid")
    void autoBid_triggerOnCounterBid() throws Exception {
        try (var seller = connect(); var autoBidder = connect(); var otherBidder = connect()) {
            String sellerId = registerAndLogin(seller, "sellerAB", "Pass@123", "sab@t.com", UserRole.SELLER);
            String autoId = registerAndLogin(autoBidder, "autoBidder", "Pass@123", "auto@t.com", UserRole.BIDDER);
            String otherId = registerAndLogin(otherBidder, "otherBidder", "Pass@123", "other@t.com", UserRole.BIDDER);

            Map<String, String> attrs = makeAttrs("brand", "Sony", "model", "PS5", "warrantyMonths", "24", "durationMinutes", "60");
            seller.sendReceive(new ClientRequest(Action.CREATE_ITEM,
                    new CreateItemRequest("Console", "PS5", 10000, sellerId, ItemType.ELECTRONICS, attrs)));

            String auctionId = DataStore.getInstance().getAuctions().stream().findFirst().orElseThrow().getId();
            Auction auction = DataStore.getInstance().getAuctions().stream()
                    .filter(a -> a.getId().equals(auctionId)).findFirst().orElseThrow();
            auction.setStatus(AuctionStatus.RUNNING);

            ClientResponse regAuto = autoBidder.sendReceive(
                    new ClientRequest(Action.REGISTER_AUTO_BID,
                            new RegisterAutoBidRequest(auctionId, autoId, 50000, 2000)));
            assertTrue(regAuto.isSuccess(), "Register auto-bid: " + regAuto.getMessage());

            ClientResponse otherBid = otherBidder.sendReceive(
                    new ClientRequest(Action.PLACE_BID, new PlaceBidRequest(auctionId, otherId, 12000, false)));
            assertTrue(otherBid.isSuccess(), "Other bidder bid: " + otherBid.getMessage());

            Thread.sleep(500);

            Auction updated = DataStore.getInstance().getAuctions().stream()
                    .filter(a -> a.getId().equals(auctionId)).findFirst().orElseThrow();
            assertTrue(updated.getCurrentPrice() > 12000,
                    "Auto-bid should have raised price above " + updated.getCurrentPrice());
        }
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Anti-sniping — bid near end extends auction")
    void antiSniping_extendsAuction() throws Exception {
        try (var client = connect()) {
            String sellerId = registerAndLogin(client, "sellerAS", "Pass@123", "sas@t.com", UserRole.SELLER);
            String bidderId = registerAndLogin(client, "bidderAS", "Pass@123", "bas@t.com", UserRole.BIDDER);

            Map<String, String> attrs = makeAttrs("brand", "Nokia", "model", "3310", "warrantyMonths", "0", "durationMinutes", "60");
            client.sendReceive(new ClientRequest(Action.CREATE_ITEM,
                    new CreateItemRequest("OldPhone", "Indestructible", 100, sellerId, ItemType.ELECTRONICS, attrs)));

            String auctionId = DataStore.getInstance().getAuctions().stream().findFirst().orElseThrow().getId();
            Auction auction = DataStore.getInstance().getAuctions().stream()
                    .filter(a -> a.getId().equals(auctionId)).findFirst().orElseThrow();
            auction.setStatus(AuctionStatus.RUNNING);
            java.time.LocalDateTime shortEnd = java.time.LocalDateTime.now().plusSeconds(25);
            auction.setEndTime(shortEnd);

            client.sendReceive(new ClientRequest(Action.PLACE_BID, new PlaceBidRequest(auctionId, bidderId, 200, false)));

            Auction updated = DataStore.getInstance().getAuctions().stream()
                    .filter(a -> a.getId().equals(auctionId)).findFirst().orElseThrow();
            assertTrue(updated.getEndTime().isAfter(shortEnd),
                    "End time should be extended after bid near end");
        }
    }

    @Test
    @Order(8)
    @DisplayName("E2E: Admin cancel auction")
    void adminCancelAuction() throws Exception {
        try (var seller = connect(); var admin = connect()) {
            registerAndLogin(seller, "sellerC", "Pass@123", "sc@t.com", UserRole.SELLER);
            String adminId = registerAndLogin(admin, "adminC", "Admin@123", "ac@t.com", UserRole.ADMIN);

            ClientResponse cancelNonexistent = admin.sendReceive(
                    new ClientRequest(Action.CANCEL_AUCTION, new CancelAuctionRequest(adminId, "fake-id", "test")));
            assertFalse(cancelNonexistent.isSuccess());

            Map<String, String> attrs = makeAttrs("brand", "B", "model", "M", "warrantyMonths", "6", "durationMinutes", "60");
            seller.sendReceive(new ClientRequest(Action.CREATE_ITEM,
                    new CreateItemRequest("ToCancel", "desc", 1000, "sellerC", ItemType.ELECTRONICS, attrs)));

            String auctionId = DataStore.getInstance().getAuctions().stream().findFirst().orElseThrow().getId();

            ClientResponse cancel = admin.sendReceive(
                    new ClientRequest(Action.CANCEL_AUCTION, new CancelAuctionRequest(adminId, auctionId, "Test cancel")));
            assertTrue(cancel.isSuccess(), "Admin cancel: " + cancel.getMessage());

            Auction cancelled = DataStore.getInstance().getAuctions().stream()
                    .filter(a -> a.getId().equals(auctionId)).findFirst().orElseThrow();
            assertEquals(AuctionStatus.CANCELED, cancelled.getStatus());
        }
    }

    @Test
    @Order(9)
    @DisplayName("E2E: Admin ban user -> banned user cannot login")
    void adminBanUser() throws Exception {
        try (var admin = connect(); var user = connect()) {
            registerAndLogin(user, "baduser", "Pass@123", "bu@t.com", UserRole.BIDDER);
            String adminId = registerAndLogin(admin, "adminB", "Admin@123", "ab@t.com", UserRole.ADMIN);

            User badUser = DataStore.getInstance().getUsers().stream()
                    .filter(u -> u.getUsername().equals("baduser")).findFirst().orElseThrow();

            ClientResponse ban = admin.sendReceive(
                    new ClientRequest(Action.BAN_USER, new BanUserRequest(adminId, badUser.getId(), "Spam")));
            assertTrue(ban.isSuccess());

            ClientResponse loginAttempt = login(user, "baduser", "Pass@123");
            assertFalse(loginAttempt.isSuccess());
            assertTrue(loginAttempt.getMessage().toLowerCase().contains("ban"));
        }
    }

    @Test
    @Order(10)
    @DisplayName("E2E: Non-admin cannot call admin actions")
    void permissionDenied() throws Exception {
        try (var client = connect()) {
            String bidderId = registerAndLogin(client, "regularUser", "Pass@123", "ru@t.com", UserRole.BIDDER);

            ClientResponse getUsers = client.sendReceive(
                    new ClientRequest(Action.GET_USERS, new GetAllUsersRequest(bidderId)));
            assertFalse(getUsers.isSuccess());
            assertTrue(getUsers.getMessage().toUpperCase().contains("PERMISSION"));

            ClientResponse cancel = client.sendReceive(
                    new ClientRequest(Action.CANCEL_AUCTION, new CancelAuctionRequest(bidderId, "any", "no")));
            assertFalse(cancel.isSuccess());
            assertTrue(cancel.getMessage().toUpperCase().contains("PERMISSION"));
        }
    }

    @Test
    @Order(11)
    @DisplayName("E2E: Get bid history returns correct auction bids")
    void getBidHistory_correctFilter() throws Exception {
        try (var client = connect()) {
            String sellerId = registerAndLogin(client, "sellerH", "Pass@123", "sh@t.com", UserRole.SELLER);
            String bidderId = registerAndLogin(client, "bidderH", "Pass@123", "bh@t.com", UserRole.BIDDER);

            Map<String, String> attrs = makeAttrs("brand", "X", "model", "Y", "warrantyMonths", "12", "durationMinutes", "60");
            client.sendReceive(new ClientRequest(Action.CREATE_ITEM,
                    new CreateItemRequest("ItemA", "desc", 1000, sellerId, ItemType.ELECTRONICS, attrs)));
            client.sendReceive(new ClientRequest(Action.CREATE_ITEM,
                    new CreateItemRequest("ItemB", "desc", 2000, sellerId, ItemType.ELECTRONICS, attrs)));

            List<Auction> allAuctions = DataStore.getInstance().getAuctions();
            assertEquals(2, allAuctions.size());
            String auctionAId = allAuctions.get(0).getId();
            String auctionBId = allAuctions.get(1).getId();
            allAuctions.forEach(a -> a.setStatus(AuctionStatus.RUNNING));

            client.sendReceive(new ClientRequest(Action.PLACE_BID, new PlaceBidRequest(auctionAId, bidderId, 1500, false)));
            client.sendReceive(new ClientRequest(Action.PLACE_BID, new PlaceBidRequest(auctionBId, bidderId, 2500, false)));

            List<?> historyA = (List<?>) client.sendReceive(
                    new ClientRequest(Action.GET_BID_HISTORY, new GetBidHistoryRequest(auctionAId))).getData();
            assertEquals(1, historyA.size(), "Auction A should have 1 bid");

            List<?> historyB = (List<?>) client.sendReceive(
                    new ClientRequest(Action.GET_BID_HISTORY, new GetBidHistoryRequest(auctionBId))).getData();
            assertEquals(1, historyB.size(), "Auction B should have 1 bid");

            BidTransaction bidA = (BidTransaction) historyA.get(0);
            assertEquals(auctionAId, bidA.getAuctionId());
            assertEquals(1500, bidA.getBidAmount());
        }
    }
}
