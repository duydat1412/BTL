package com.auction.server.repository;

import com.auction.common.entity.*;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.ItemType;
import com.auction.common.enums.UserRole;
import com.auction.server.datastore.DataStore;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    @BeforeEach
    void setUp() {
        DataStore.getInstance().getUsers().clear();
        DataStore.getInstance().getItems().clear();
        DataStore.getInstance().getAuctions().clear();
        DataStore.getInstance().getBidTransactions().clear();
    }

    @AfterEach
    void tearDown() {
        DataStore.getInstance().getUsers().clear();
        DataStore.getInstance().getItems().clear();
        DataStore.getInstance().getAuctions().clear();
        DataStore.getInstance().getBidTransactions().clear();
    }

    @Nested
    @DisplayName("SerializableUserRepository tests")
    class UserRepoTests {

        private SerializableUserRepository repo;

        @BeforeEach
        void init() {
            repo = new SerializableUserRepository();
        }

        @Test
        @DisplayName("Save and find by ID")
        void saveAndFindById() {
            Bidder user = new Bidder("testuser", "hash", "test@test.com");
            repo.save(user);
            assertNotNull(repo.findById(user.getId()));
        }

        @Test
        @DisplayName("Find by username")
        void findByUsername() {
            Bidder user = new Bidder("uniqueuser", "hash", "u@test.com");
            repo.save(user);
            assertNotNull(repo.findByUsername("uniqueuser"));
            assertNull(repo.findByUsername("nonexistent"));
        }

        @Test
        @DisplayName("Find all returns all users")
        void findAll() {
            repo.save(new Bidder("u1", "h", "u1@t.com"));
            repo.save(new Seller("u2", "h", "u2@t.com"));
            assertEquals(2, repo.findAll().size());
        }

        @Test
        @DisplayName("Delete removes user")
        void delete() {
            Bidder user = new Bidder("todelete", "hash", "del@t.com");
            repo.save(user);
            repo.delete(user.getId());
            assertNull(repo.findById(user.getId()));
        }
    }

    @Nested
    @DisplayName("SerializableItemRepository tests")
    class ItemRepoTests {

        private SerializableItemRepository repo;

        @BeforeEach
        void init() {
            repo = new SerializableItemRepository();
        }

        @Test
        @DisplayName("Save and find by ID")
        void saveAndFindById() {
            Electronics item = new Electronics();
            item.setName("Phone");
            repo.save(item);
            assertNotNull(repo.findById(item.getId()));
        }

        @Test
        @DisplayName("Find all returns all items")
        void findAll() {
            repo.save(new Electronics());
            repo.save(new Art());
            assertEquals(2, repo.findAll().size());
        }

        @Test
        @DisplayName("Delete removes item")
        void delete() {
            Electronics item = new Electronics();
            item.setName("ToDelete");
            repo.save(item);
            repo.delete(item.getId());
            assertNull(repo.findById(item.getId()));
        }

        @Test
        @DisplayName("Find by non-existent ID returns null")
        void findById_notFound() {
            assertNull(repo.findById("nonexistent"));
        }
    }

    @Nested
    @DisplayName("SerializableAuctionRepository tests")
    class AuctionRepoTests {

        private SerializableAuctionRepository repo;

        @BeforeEach
        void init() {
            repo = new SerializableAuctionRepository();
        }

        @Test
        @DisplayName("Save and find by ID")
        void saveAndFindById() {
            Auction auction = new Auction("item-1", "seller-1", "Test", 1000,
                    java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusHours(1));
            auction.setStatus(AuctionStatus.OPEN);
            repo.save(auction);
            assertNotNull(repo.findById(auction.getId()));
        }

        @Test
        @DisplayName("Find all returns all auctions")
        void findAll() {
            Auction a1 = new Auction("i1", "s1", "A", 100, java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusHours(1));
            Auction a2 = new Auction("i2", "s2", "B", 200, java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusHours(1));
            repo.save(a1);
            repo.save(a2);
            assertEquals(2, repo.findAll().size());
        }

        @Test
        @DisplayName("Delete removes auction")
        void delete() {
            Auction auction = new Auction("item-del", "seller-del", "ToDelete", 100,
                    java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusHours(1));
            repo.save(auction);
            repo.delete(auction.getId());
            assertNull(repo.findById(auction.getId()));
        }
    }

    @Nested
    @DisplayName("SerializableBidRepository tests")
    class BidRepoTests {

        private SerializableBidRepository repo;

        @BeforeEach
        void init() {
            repo = new SerializableBidRepository();
        }

        @Test
        @DisplayName("Save bid transaction")
        void save() {
            BidTransaction bid = new BidTransaction("auction-1", "bidder-1", 1000);
            repo.save(bid);
            assertEquals(1, DataStore.getInstance().getBidTransactions().size());
        }

        @Test
        @DisplayName("Find by auction ID returns only matching bids")
        void findByAuctionId_filtersCorrectly() {
            BidTransaction bid1 = new BidTransaction("auction-A", "bidder-1", 1000);
            BidTransaction bid2 = new BidTransaction("auction-A", "bidder-2", 2000);
            BidTransaction bid3 = new BidTransaction("auction-B", "bidder-1", 1500);
            repo.save(bid1);
            repo.save(bid2);
            repo.save(bid3);

            assertEquals(2, repo.findByAuctionId("auction-A").size());
            assertEquals(1, repo.findByAuctionId("auction-B").size());
            assertTrue(repo.findByAuctionId("unknown").isEmpty());
        }

        @Test
        @DisplayName("Empty result when no bids exist")
        void findByAuctionId_empty() {
            assertTrue(repo.findByAuctionId("any").isEmpty());
        }
    }
}
