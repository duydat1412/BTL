package com.auction.server.observer;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.observer.AuctionObserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho AuctionEventManager — kiểm tra Observer Pattern.
 */
class AuctionEventManagerTest {

    private AuctionEventManager eventManager;
    private int newBidCount;
    private int statusChangedCount;
    private int auctionEndedCount;

    @BeforeEach
    void setUp() {
        eventManager = new AuctionEventManager();
        newBidCount = 0;
        statusChangedCount = 0;
        auctionEndedCount = 0;
    }

    @Test
    @DisplayName("subscribe + notifyNewBid → observer nhận thông báo")
    void subscribe_notifyNewBid_observerReceivesNotification() {
        eventManager.subscribe(createCountingObserver());

        Auction auction = createAuction();
        BidTransaction bid = new BidTransaction("a1", "b1", 1500);
        eventManager.notifyNewBid(auction, bid);

        assertEquals(1, newBidCount);
    }

    @Test
    @DisplayName("subscribe nhiều observer → tất cả đều nhận thông báo")
    void subscribe_multipleObservers_allNotified() {
        eventManager.subscribe(createCountingObserver());
        eventManager.subscribe(createCountingObserver());

        eventManager.notifyNewBid(createAuction(),
                new BidTransaction("a1", "b1", 1500));

        assertEquals(2, newBidCount);
    }

    @Test
    @DisplayName("unsubscribe → observer không nhận thông báo nữa")
    void unsubscribe_observerStopsReceiving() {
        AuctionObserver observer = createCountingObserver();
        eventManager.subscribe(observer);
        eventManager.unsubscribe(observer);

        eventManager.notifyNewBid(createAuction(),
                new BidTransaction("a1", "b1", 1500));

        assertEquals(0, newBidCount);
    }

    @Test
    @DisplayName("notifyStatusChanged → observer nhận đúng oldStatus và newStatus")
    void notifyStatusChanged_observerReceivesCorrectStatuses() {
        final AuctionStatus[] receivedOld = {null};
        final AuctionStatus[] receivedNew = {null};

        eventManager.subscribe(new AuctionObserver() {
            @Override
            public void onNewBid(Auction a, BidTransaction bid) { }

            @Override
            public void onAuctionStatusChanged(Auction a,
                    AuctionStatus oldStatus, AuctionStatus newStatus) {
                receivedOld[0] = oldStatus;
                receivedNew[0] = newStatus;
            }

            @Override
            public void onAuctionEnded(Auction a) { }
        });

        eventManager.notifyStatusChanged(createAuction(),
                AuctionStatus.OPEN, AuctionStatus.RUNNING);

        assertEquals(AuctionStatus.OPEN, receivedOld[0]);
        assertEquals(AuctionStatus.RUNNING, receivedNew[0]);
    }

    @Test
    @DisplayName("notifyAuctionEnded → observer nhận thông báo kết thúc")
    void notifyAuctionEnded_observerNotified() {
        eventManager.subscribe(createCountingObserver());
        eventManager.notifyAuctionEnded(createAuction());
        assertEquals(1, auctionEndedCount);
    }

    @Test
    @DisplayName("getObserverCount → trả về đúng số lượng")
    void getObserverCount_returnsCorrectCount() {
        assertEquals(0, eventManager.getObserverCount());

        AuctionObserver obs = createCountingObserver();
        eventManager.subscribe(obs);
        assertEquals(1, eventManager.getObserverCount());

        eventManager.unsubscribe(obs);
        assertEquals(0, eventManager.getObserverCount());
    }

    @Test
    @DisplayName("subscribe(null) → không thêm vào danh sách")
    void subscribe_null_ignored() {
        eventManager.subscribe(null);
        assertEquals(0, eventManager.getObserverCount());
    }

    @Test
    @DisplayName("subscribe cùng observer 2 lần → chỉ thêm 1 lần")
    void subscribe_duplicate_ignoredSecondTime() {
        AuctionObserver obs = createCountingObserver();
        eventManager.subscribe(obs);
        eventManager.subscribe(obs);
        assertEquals(1, eventManager.getObserverCount());
    }

    // ==================== Helpers ====================

    private AuctionObserver createCountingObserver() {
        return new AuctionObserver() {
            @Override
            public void onNewBid(Auction a, BidTransaction bid) {
                newBidCount++;
            }

            @Override
            public void onAuctionStatusChanged(Auction a,
                    AuctionStatus oldStatus, AuctionStatus newStatus) {
                statusChangedCount++;
            }

            @Override
            public void onAuctionEnded(Auction a) {
                auctionEndedCount++;
            }
        };
    }

    private Auction createAuction() {
        return new Auction("item-1", "seller-1", "Test",
                1000, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
    }
}
