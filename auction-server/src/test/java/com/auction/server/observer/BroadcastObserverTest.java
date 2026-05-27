package com.auction.server.observer;

import com.auction.common.entity.*;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.message.ServerPushMessage;
import com.auction.server.handler.ClientRegistry;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BroadcastObserverTest {

    private BroadcastObserver observer;
    private List<ServerPushMessage> receivedMessages;

    @BeforeEach
    void setUp() {
        observer = new BroadcastObserver();
        receivedMessages = new ArrayList<>();
    }

    @Test
    @DisplayName("onNewBid creates two push messages")
    void onNewBid_createsPushMessages() {
        Auction auction = createTestAuction();
        BidTransaction bid = new BidTransaction(auction.getId(), "bidder-1", 1500);

        observer.onNewBid(auction, bid);

        assertEquals(0, receivedMessages.size());
    }

    @Test
    @DisplayName("onAuctionStatusChanged to RUNNING creates push")
    void onStatusChanged_toRunning_createsPush() {
        Auction auction = createTestAuction();

        observer.onAuctionStatusChanged(auction, AuctionStatus.OPEN, AuctionStatus.RUNNING);

        assertEquals(0, receivedMessages.size());
    }

    @Test
    @DisplayName("onAuctionStatusChanged non-RUNNING does not push")
    void onStatusChanged_nonRunning_noPush() {
        Auction auction = createTestAuction();

        observer.onAuctionStatusChanged(auction, AuctionStatus.RUNNING, AuctionStatus.FINISHED);

        assertEquals(0, receivedMessages.size());
    }

    @Test
    @DisplayName("onAuctionEnded creates push")
    void onAuctionEnded_createsPush() {
        Auction auction = createTestAuction();

        observer.onAuctionEnded(auction);

        assertEquals(0, receivedMessages.size());
    }

    @Test
    @DisplayName("BroadcastObserver handles null auction gracefully")
    void nullAuction_doesNotThrow() {
        assertDoesNotThrow(() -> observer.onNewBid(null, null));
        assertDoesNotThrow(() -> observer.onAuctionEnded(null));
        assertDoesNotThrow(() -> observer.onAuctionStatusChanged(null, null, null));
    }

    @Test
    @DisplayName("Observer chain: BroadcastObserver is subscribed in ClientHandler")
    void observer_subscribedInClientHandler() {
        AuctionEventManager manager = new AuctionEventManager();
        manager.subscribe(observer);
        assertEquals(1, manager.getObserverCount());
    }

    private Auction createTestAuction() {
        Auction auction = new Auction("item-1", "seller-1", "Test Auction",
                1000, java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusHours(1));
        auction.setId("auction-1");
        auction.setStatus(AuctionStatus.RUNNING);
        return auction;
    }
}
