package com.auction.server.datastore;

import com.auction.common.entity.User;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class DataStoreTest {

    @BeforeEach
    void setUp() {
        DataStore.getInstance().getUsers().clear();
        DataStore.getInstance().getItems().clear();
        DataStore.getInstance().getAuctions().clear();
        DataStore.getInstance().getBidTransactions().clear();
    }

    @Test
    @DisplayName("DataStore is singleton")
    void dataStore_isSingleton() {
        DataStore instance1 = DataStore.getInstance();
        DataStore instance2 = DataStore.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("DataStore starts with empty lists")
    void dataStore_startsWithEmptyLists() {
        assertTrue(DataStore.getInstance().getUsers().isEmpty());
        assertTrue(DataStore.getInstance().getItems().isEmpty());
        assertTrue(DataStore.getInstance().getAuctions().isEmpty());
        assertTrue(DataStore.getInstance().getBidTransactions().isEmpty());
    }

    @Test
    @DisplayName("Can add and retrieve users")
    void dataStore_addAndRetrieveUsers() {
        DataStore ds = DataStore.getInstance();
        assertEquals(0, ds.getUsers().size());
        ds.getUsers().add(new com.auction.common.entity.Bidder("testuser", "hash", "test@test.com"));
        assertEquals(1, ds.getUsers().size());
        assertEquals("testuser", ds.getUsers().get(0).getUsername());
    }

    @Test
    @DisplayName("Can add and retrieve items")
    void dataStore_addAndRetrieveItems() {
        DataStore ds = DataStore.getInstance();
        com.auction.common.entity.Electronics item = new com.auction.common.entity.Electronics();
        item.setName("Laptop");
        ds.getItems().add(item);
        assertEquals(1, ds.getItems().size());
        assertEquals("Laptop", ds.getItems().get(0).getName());
    }

    @Test
    @DisplayName("Can add and retrieve auctions")
    void dataStore_addAndRetrieveAuctions() {
        DataStore ds = DataStore.getInstance();
        com.auction.common.entity.Auction auction = new com.auction.common.entity.Auction();
        auction.setTitle("Test Auction");
        ds.getAuctions().add(auction);
        assertEquals(1, ds.getAuctions().size());
    }

    @Test
    @DisplayName("Can add and retrieve bid transactions")
    void dataStore_addAndRetrieveBids() {
        DataStore ds = DataStore.getInstance();
        com.auction.common.entity.BidTransaction bid = new com.auction.common.entity.BidTransaction(
                "auction-1", "bidder-1", 1000
        );
        ds.getBidTransactions().add(bid);
        assertEquals(1, ds.getBidTransactions().size());
    }

    @Test
    @DisplayName("DataStore does not create default admin when admin exists")
    void dataStore_skipDefaultAdminIfExists() {
        DataStore ds = DataStore.getInstance();
        com.auction.common.entity.Admin existingAdmin = new com.auction.common.entity.Admin(
                "existing", "hash", "exist@test.com", "IT");
        ds.getUsers().add(existingAdmin);

        int beforeCount = ds.getUsers().size();
        DataStore.getInstance().loadData();
        assertEquals(beforeCount, ds.getUsers().size());
    }
}
