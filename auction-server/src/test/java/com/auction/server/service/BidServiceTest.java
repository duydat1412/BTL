package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.strategy.BidStrategy;
import com.auction.common.strategy.ManualBidStrategy;
import com.auction.server.exception.AuctionClosedException;
import com.auction.server.exception.InvalidBidException;
import com.auction.server.observer.AuctionEventManager;
import com.auction.server.repository.AuctionRepository;
import com.auction.server.repository.BidRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho BidService — kiểm tra logic đặt giá.
 *
 * <p>Sử dụng stub repositories (không phụ thuộc DataStore/file)
 * để test riêng logic BidService.
 */
class BidServiceTest {

    private BidService bidService;
    private StubAuctionRepository auctionRepository;
    private StubBidRepository bidRepository;
    private AuctionEventManager eventManager;
    private BidStrategy manualStrategy;

    @BeforeEach
    void setUp() {
        auctionRepository = new StubAuctionRepository();
        bidRepository = new StubBidRepository();
        eventManager = new AuctionEventManager();
        bidService = new BidService(auctionRepository, bidRepository, eventManager);
        manualStrategy = new ManualBidStrategy();
    }

    // ==================== placeBid — Happy Path ====================

    @Test
    @DisplayName("placeBid: bid hợp lệ → tạo BidTransaction + cập nhật Auction")
    void placeBid_validBid_success() throws AuctionClosedException, InvalidBidException {
        // Arrange
        Auction auction = createRunningAuction("auction-1", 1000);
        auctionRepository.save(auction);

        // Act
        BidTransaction result = bidService.placeBid(
                "auction-1", "bidder-1", 1500, manualStrategy);

        // Assert
        assertNotNull(result);
        assertEquals("auction-1", result.getAuctionId());
        assertEquals("bidder-1", result.getBidderId());
        assertEquals(1500, result.getBidAmount());

        // Auction phải được cập nhật
        Auction updated = auctionRepository.findById("auction-1");
        assertEquals(1500, updated.getCurrentPrice());
        assertEquals("bidder-1", updated.getHighestBidderId());
    }

    @Test
    @DisplayName("placeBid: bid liên tiếp → giá tăng dần, highestBidder cập nhật")
    void placeBid_multipleBids_priceIncreases()
            throws AuctionClosedException, InvalidBidException {
        // Arrange
        Auction auction = createRunningAuction("auction-1", 1000);
        auctionRepository.save(auction);

        // Act
        bidService.placeBid("auction-1", "bidder-1", 1500, manualStrategy);
        bidService.placeBid("auction-1", "bidder-2", 2000, manualStrategy);
        bidService.placeBid("auction-1", "bidder-3", 2500, manualStrategy);

        // Assert
        Auction updated = auctionRepository.findById("auction-1");
        assertEquals(2500, updated.getCurrentPrice());
        assertEquals("bidder-3", updated.getHighestBidderId());
        assertEquals(3, bidRepository.findByAuctionId("auction-1").size());
    }

    // ==================== placeBid — Error Cases ====================

    @Test
    @DisplayName("placeBid: auction không tồn tại → InvalidBidException")
    void placeBid_auctionNotFound_throwsInvalidBid() {
        assertThrows(InvalidBidException.class, () ->
                bidService.placeBid("non-existent", "bidder-1", 1500, manualStrategy));
    }

    @Test
    @DisplayName("placeBid: auction đã FINISHED → AuctionClosedException")
    void placeBid_auctionFinished_throwsAuctionClosed() {
        // Arrange
        Auction auction = createRunningAuction("auction-1", 1000);
        auction.setStatus(AuctionStatus.FINISHED);
        auctionRepository.save(auction);

        // Act & Assert
        assertThrows(AuctionClosedException.class, () ->
                bidService.placeBid("auction-1", "bidder-1", 1500, manualStrategy));
    }

    @Test
    @DisplayName("placeBid: auction OPEN (chưa bắt đầu) → AuctionClosedException")
    void placeBid_auctionOpen_throwsAuctionClosed() {
        Auction auction = createRunningAuction("auction-1", 1000);
        auction.setStatus(AuctionStatus.OPEN);
        auctionRepository.save(auction);

        assertThrows(AuctionClosedException.class, () ->
                bidService.placeBid("auction-1", "bidder-1", 1500, manualStrategy));
    }

    @Test
    @DisplayName("placeBid: giá thấp hơn currentPrice → InvalidBidException")
    void placeBid_bidTooLow_throwsInvalidBid() {
        Auction auction = createRunningAuction("auction-1", 1000);
        auctionRepository.save(auction);

        assertThrows(InvalidBidException.class, () ->
                bidService.placeBid("auction-1", "bidder-1", 500, manualStrategy));
    }

    @Test
    @DisplayName("placeBid: giá bằng currentPrice → InvalidBidException")
    void placeBid_bidEqualToCurrentPrice_throwsInvalidBid() {
        Auction auction = createRunningAuction("auction-1", 1000);
        auctionRepository.save(auction);

        assertThrows(InvalidBidException.class, () ->
                bidService.placeBid("auction-1", "bidder-1", 1000, manualStrategy));
    }

    // ==================== getBidHistory ====================

    @Test
    @DisplayName("getBidHistory: trả về danh sách bid của auction")
    void getBidHistory_returnsBids() throws AuctionClosedException, InvalidBidException {
        Auction auction = createRunningAuction("auction-1", 1000);
        auctionRepository.save(auction);

        bidService.placeBid("auction-1", "bidder-1", 1500, manualStrategy);
        bidService.placeBid("auction-1", "bidder-2", 2000, manualStrategy);

        List<BidTransaction> history = bidService.getBidHistory("auction-1");
        assertEquals(2, history.size());
    }

    // ==================== Observer Notification ====================

    @Test
    @DisplayName("placeBid: observer được thông báo khi có bid mới")
    void placeBid_notifiesObserver() throws AuctionClosedException, InvalidBidException {
        // Arrange
        Auction auction = createRunningAuction("auction-1", 1000);
        auctionRepository.save(auction);

        // Tạo observer đếm số lần nhận thông báo
        final int[] notifyCount = {0};
        eventManager.subscribe(new com.auction.common.observer.AuctionObserver() {
            @Override
            public void onNewBid(Auction a, BidTransaction bid) {
                notifyCount[0]++;
            }

            @Override
            public void onAuctionStatusChanged(Auction a,
                    AuctionStatus oldStatus, AuctionStatus newStatus) { }

            @Override
            public void onAuctionEnded(Auction a) { }
        });

        // Act
        bidService.placeBid("auction-1", "bidder-1", 1500, manualStrategy);

        // Assert
        assertEquals(1, notifyCount[0]);
    }

    // ==================== Helper Methods ====================

    /**
     * Tạo Auction đang RUNNING với giá khởi điểm cho test.
     */
    private Auction createRunningAuction(String id, double startPrice) {
        Auction auction = new Auction("item-1", "seller-1", "Test Auction",
                startPrice, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        auction.setId(id);
        auction.setStatus(AuctionStatus.RUNNING);
        return auction;
    }

    // ==================== Stub Repositories ====================

    /**
     * Stub AuctionRepository — lưu trong memory (HashMap).
     * Không phụ thuộc DataStore/file, phục vụ unit test.
     */
    private static class StubAuctionRepository implements AuctionRepository {
        private final Map<String, Auction> store = new HashMap<>();

        @Override
        public void save(Auction auction) {
            store.put(auction.getId(), auction);
        }

        @Override
        public Auction findById(String id) {
            return store.get(id);
        }

        @Override
        public List<Auction> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public void update(Auction auction) {
            store.put(auction.getId(), auction);
        }

        @Override
        public void delete(String id) {
            store.remove(id);
        }
    }

    /**
     * Stub BidRepository — lưu trong memory (ArrayList).
     */
    private static class StubBidRepository implements BidRepository {
        private final List<BidTransaction> store = new ArrayList<>();

        @Override
        public void save(BidTransaction bid) {
            store.add(bid);
        }

        @Override
        public List<BidTransaction> findByAuctionId(String auctionId) {
            List<BidTransaction> result = new ArrayList<>();
            for (BidTransaction bid : store) {
                if (bid.getAuctionId().equals(auctionId)) {
                    result.add(bid);
                }
            }
            return result;
        }
    }
}
