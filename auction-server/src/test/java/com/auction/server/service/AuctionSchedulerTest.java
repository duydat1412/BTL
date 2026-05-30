package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.entity.Electronics;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.ItemType;
import com.auction.common.observer.AuctionObserver;
import com.auction.server.datastore.DataStore;
import com.auction.server.observer.AuctionEventManager;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AuctionSchedulerTest {

    private AuctionEventManager eventManager;
    private Auction testAuction;

    @BeforeEach
    void setUp() {
        AuctionScheduler.resetForTests();
        DataStore.getInstance().getAuctions().clear();
        DataStore.getInstance().getItems().clear();

        Electronics item = new Electronics();
        item.setName("Test Phone");
        item.setStartingPrice(1000);
        item.setSellerId("seller-1");
        item.setItemType(ItemType.ELECTRONICS);
        DataStore.getInstance().getItems().add(item);

        testAuction = new Auction(item.getId(), item.getSellerId(), "Test Auction",
                1000, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));
        testAuction.setStatus(AuctionStatus.OPEN);
        DataStore.getInstance().getAuctions().add(testAuction);

        eventManager = new AuctionEventManager();
        AuctionScheduler.setEventManager(eventManager);
    }

    @AfterEach
    void tearDown() {
        AuctionScheduler.resetForTests();
        DataStore.getInstance().getAuctions().clear();
        DataStore.getInstance().getItems().clear();
    }

    @Test
    @DisplayName("scheduleAuctionStart with past time starts immediately")
    void scheduleStart_pastTime_startsNow() {
        AuctionScheduler.scheduleAuctionStart(testAuction.getId(), LocalDateTime.now().minusMinutes(5));
        Auction updated = DataStore.getInstance().getAuctions().stream()
                .filter(a -> a.getId().equals(testAuction.getId())).findFirst().orElseThrow();
        assertEquals(AuctionStatus.RUNNING, updated.getStatus());
    }

    @Test
    @DisplayName("scheduleAuctionEndAt with past time finishes immediately")
    void scheduleEndAt_pastTime_finishesNow() {
        testAuction.setStatus(AuctionStatus.RUNNING);
        AuctionScheduler.scheduleAuctionEndAt(testAuction.getId(), LocalDateTime.now().minusMinutes(5));
        Auction updated = DataStore.getInstance().getAuctions().stream()
                .filter(a -> a.getId().equals(testAuction.getId())).findFirst().orElseThrow();
        assertEquals(AuctionStatus.FINISHED, updated.getStatus());
    }

    @Test
    @DisplayName("Scheduler notifies observer on status change")
    void scheduler_notifiesObserver() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<AuctionStatus> notifiedStatus = new AtomicReference<>();

        eventManager.subscribe(new AuctionObserver() {
            @Override
            public void onNewBid(Auction auction, com.auction.common.entity.BidTransaction bid) {}

            @Override
            public void onAuctionStatusChanged(Auction auction, AuctionStatus oldStatus, AuctionStatus newStatus) {
                notifiedStatus.set(newStatus);
                latch.countDown();
            }

            @Override
            public void onAuctionEnded(Auction auction) {}
        });

        AuctionScheduler.scheduleAuctionStart(testAuction.getId(), LocalDateTime.now().minusMinutes(1));
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(AuctionStatus.RUNNING, notifiedStatus.get());
    }

    @Test
    @DisplayName("Scheduler notifies auction ended on finish")
    void scheduler_notifiesAuctionEnded() throws InterruptedException {
        testAuction.setStatus(AuctionStatus.RUNNING);
        CountDownLatch latch = new CountDownLatch(1);
        eventManager.subscribe(new AuctionObserver() {
            @Override
            public void onNewBid(Auction auction, com.auction.common.entity.BidTransaction bid) {}

            @Override
            public void onAuctionStatusChanged(Auction auction, AuctionStatus oldStatus, AuctionStatus newStatus) {}

            @Override
            public void onAuctionEnded(Auction auction) {
                latch.countDown();
            }
        });

        AuctionScheduler.scheduleAuctionEndAt(testAuction.getId(), LocalDateTime.now().minusMinutes(1));
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Scheduled start ignores non-OPEN auctions")
    void scheduleStart_nonOpen_ignored() {
        testAuction.setStatus(AuctionStatus.RUNNING);
        AuctionScheduler.scheduleAuctionStart(testAuction.getId(), LocalDateTime.now().minusMinutes(5));
        Auction updated = DataStore.getInstance().getAuctions().stream()
                .filter(a -> a.getId().equals(testAuction.getId())).findFirst().orElseThrow();
        assertEquals(AuctionStatus.RUNNING, updated.getStatus());
    }

    @Test
    @DisplayName("Scheduled end ignores non-RUNNING auctions")
    void scheduleEnd_nonRunning_ignored() {
        AuctionScheduler.scheduleAuctionEndAt(testAuction.getId(), LocalDateTime.now().minusMinutes(5));
        Auction updated = DataStore.getInstance().getAuctions().stream()
                .filter(a -> a.getId().equals(testAuction.getId())).findFirst().orElseThrow();
        assertEquals(AuctionStatus.OPEN, updated.getStatus());
    }

    @Test
    @DisplayName("scheduleAuctionEnd with duration completes")
    void scheduleEnd_duration_success() throws InterruptedException {
        testAuction.setStatus(AuctionStatus.RUNNING);
        CountDownLatch latch = new CountDownLatch(1);
        eventManager.subscribe(new AuctionObserver() {
            @Override
            public void onNewBid(Auction auction, com.auction.common.entity.BidTransaction bid) {}
            @Override
            public void onAuctionStatusChanged(Auction auction, AuctionStatus oldStatus, AuctionStatus newStatus) {
                if (newStatus == AuctionStatus.FINISHED) latch.countDown();
            }
            @Override
            public void onAuctionEnded(Auction auction) {}
        });

        AuctionScheduler.scheduleAuctionEnd(testAuction.getId(), 0);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}
