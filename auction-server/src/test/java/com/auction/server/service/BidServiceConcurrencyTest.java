package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.strategy.ManualBidStrategy;
import com.auction.server.exception.AuctionClosedException;
import com.auction.server.exception.InvalidBidException;
import com.auction.server.observer.AuctionEventManager;
import com.auction.server.repository.AuctionRepository;
import com.auction.server.repository.BidRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test concurrency cho BidService — kiểm tra ReentrantLock hoạt động đúng.
 *
 * <p>Dùng {@link ExecutorService} + {@link CountDownLatch} để mô phỏng
 * nhiều bidder đặt giá cùng lúc trên 1 auction.
 *
 * <p>Expected: không có race condition, chỉ 1 highestBidder cuối cùng,
 * giá tăng tuần tự, không có lost update.
 */
class BidServiceConcurrencyTest {

    private BidService bidService;
    private StubAuctionRepository auctionRepository;
    private StubBidRepository bidRepository;

    @BeforeEach
    void setUp() {
        auctionRepository = new StubAuctionRepository();
        bidRepository = new StubBidRepository();
        AuctionEventManager eventManager = new AuctionEventManager();
        bidService = new BidService(auctionRepository, bidRepository, eventManager);
    }

    @Test
    @DisplayName("Concurrent bids: 10 thread đặt giá cùng lúc → không race condition")
    void concurrentBids_shouldNotCauseRaceCondition() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        Auction auction = createRunningAuction("auction-1", 1000);
        auctionRepository.save(auction);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // Act — mỗi bidder đặt giá tăng dần
        for (int i = 0; i < threadCount; i++) {
            final int bidderId = i;
            final double bidAmount = 1000 + (bidderId + 1) * 500; // 1500, 2000, ..., 6000
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await(); // Đợi tất cả sẵn sàng
                    bidService.placeBid("auction-1", "bidder-" + bidderId,
                            bidAmount, new ManualBidStrategy());
                    successCount.incrementAndGet();
                } catch (InvalidBidException | AuctionClosedException e) {
                    // Bid bị reject vì giá thấp hơn currentPrice (bình thường)
                    failCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Đợi tất cả thread sẵn sàng rồi bắn cùng lúc
        readyLatch.await();
        startLatch.countDown();

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS),
                "Executor should terminate within 10 seconds");

        // Assert
        Auction updated = auctionRepository.findById("auction-1");

        // Giá cuối cùng phải > giá khởi điểm
        assertTrue(updated.getCurrentPrice() > 1000,
                "Current price should be greater than start price");

        // Tổng success + fail phải = threadCount
        assertEquals(threadCount, successCount.get() + failCount.get(),
                "All threads should have completed (success or fail)");

        // Ít nhất 1 bid thành công
        assertTrue(successCount.get() >= 1,
                "At least 1 bid should succeed");

        // highestBidderId phải có giá trị
        assertNotNull(updated.getHighestBidderId(),
                "There should be a highest bidder");
    }

    @RepeatedTest(3)
    @DisplayName("Repeated concurrent test: chạy 3 lần để phát hiện flaky")
    void repeatedConcurrentTest_shouldBeConsistent() throws InterruptedException {
        int threadCount = 5;
        Auction auction = createRunningAuction("auction-repeat", 100);
        auctionRepository.save(auction);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final double bidAmount = 100 + (i + 1) * 100;
            final int idx = i;
            executor.submit(() -> {
                try {
                    latch.await();
                    bidService.placeBid("auction-repeat", "bidder-" + idx,
                            bidAmount, new ManualBidStrategy());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Expected: một số bid bị reject
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        Auction updated = auctionRepository.findById("auction-repeat");
        assertTrue(updated.getCurrentPrice() > 100,
                "Price should increase after concurrent bids");
        assertTrue(successCount.get() >= 1,
                "At least 1 bid should succeed");
    }

    @Test
    @DisplayName("Concurrent bids trên 2 auction khác nhau → chạy song song, không block nhau")
    void concurrentBids_differentAuctions_runInParallel() throws InterruptedException {
        // Arrange
        Auction auction1 = createRunningAuction("auction-A", 1000);
        Auction auction2 = createRunningAuction("auction-B", 2000);
        auctionRepository.save(auction1);
        auctionRepository.save(auction2);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        // Bid trên auction A
        executor.submit(() -> {
            try {
                latch.await();
                bidService.placeBid("auction-A", "bidder-1", 1500,
                        new ManualBidStrategy());
                successCount.incrementAndGet();
            } catch (Exception e) {
                fail("Bid on auction-A should succeed: " + e.getMessage());
            }
        });

        // Bid trên auction B
        executor.submit(() -> {
            try {
                latch.await();
                bidService.placeBid("auction-B", "bidder-2", 2500,
                        new ManualBidStrategy());
                successCount.incrementAndGet();
            } catch (Exception e) {
                fail("Bid on auction-B should succeed: " + e.getMessage());
            }
        });

        latch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        // Cả 2 bid đều phải thành công
        assertEquals(2, successCount.get(),
                "Both bids on different auctions should succeed");

        assertEquals(1500, auctionRepository.findById("auction-A").getCurrentPrice());
        assertEquals(2500, auctionRepository.findById("auction-B").getCurrentPrice());
    }

    // ==================== Helper ====================

    private Auction createRunningAuction(String id, double startPrice) {
        Auction auction = new Auction("item-1", "seller-1", "Test Auction",
                startPrice, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        auction.setId(id);
        auction.setStatus(AuctionStatus.RUNNING);
        return auction;
    }

    // ==================== Stub Repositories ====================

    /**
     * Thread-safe stub — dùng synchronized cho concurrent test.
     */
    private static class StubAuctionRepository implements AuctionRepository {
        private final Map<String, Auction> store = new HashMap<>();

        @Override
        public synchronized void save(Auction auction) {
            store.put(auction.getId(), auction);
        }

        @Override
        public synchronized Auction findById(String id) {
            return store.get(id);
        }

        @Override
        public synchronized List<Auction> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public synchronized void update(Auction auction) {
            store.put(auction.getId(), auction);
        }

        @Override
        public synchronized void delete(String id) {
            store.remove(id);
        }
    }

    private static class StubBidRepository implements BidRepository {
        private final List<BidTransaction> store = new ArrayList<>();

        @Override
        public synchronized void save(BidTransaction bid) {
            store.add(bid);
        }

        @Override
        public synchronized List<BidTransaction> findByAuctionId(String auctionId) {
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
