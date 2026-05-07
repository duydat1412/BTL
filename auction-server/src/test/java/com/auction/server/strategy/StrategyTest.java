package com.auction.server.strategy;

import com.auction.common.entity.Auction;
import com.auction.common.entity.AutoBid;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.strategy.AutoBidStrategy;
import com.auction.common.strategy.BidStrategy;
import com.auction.common.strategy.ManualBidStrategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho Strategy Pattern — ManualBidStrategy + AutoBidStrategy.
 */
class StrategyTest {

    // ==================== ManualBidStrategy ====================

    @Nested
    @DisplayName("ManualBidStrategy")
    class ManualBidStrategyTest {

        private final BidStrategy strategy = new ManualBidStrategy();

        @Test
        @DisplayName("calculateBid: giá hợp lệ → tạo BidTransaction đúng")
        void calculateBid_validAmount_returnsBidTransaction() {
            Auction auction = createRunningAuction(1000);
            BidTransaction bid = strategy.calculateBid(auction, "bidder-1", 1500);

            assertEquals("bidder-1", bid.getBidderId());
            assertEquals(1500, bid.getBidAmount());
            assertEquals(auction.getId(), bid.getAuctionId());
        }

        @Test
        @DisplayName("calculateBid: giá thấp hơn currentPrice → exception")
        void calculateBid_lowAmount_throwsException() {
            Auction auction = createRunningAuction(1000);
            assertThrows(IllegalArgumentException.class,
                    () -> strategy.calculateBid(auction, "bidder-1", 500));
        }

        @Test
        @DisplayName("calculateBid: auction không RUNNING → exception")
        void calculateBid_auctionNotRunning_throwsException() {
            Auction auction = createRunningAuction(1000);
            auction.setStatus(AuctionStatus.FINISHED);
            assertThrows(IllegalArgumentException.class,
                    () -> strategy.calculateBid(auction, "bidder-1", 1500));
        }

        @Test
        @DisplayName("isValidBid: giá > currentPrice + RUNNING → true")
        void isValidBid_validConditions_returnsTrue() {
            Auction auction = createRunningAuction(1000);
            assertTrue(strategy.isValidBid(auction, 1500));
        }

        @Test
        @DisplayName("isValidBid: giá = currentPrice → false")
        void isValidBid_equalPrice_returnsFalse() {
            Auction auction = createRunningAuction(1000);
            assertFalse(strategy.isValidBid(auction, 1000));
        }

        @Test
        @DisplayName("isValidBid: giá < currentPrice → false")
        void isValidBid_lowPrice_returnsFalse() {
            Auction auction = createRunningAuction(1000);
            assertFalse(strategy.isValidBid(auction, 500));
        }

        @Test
        @DisplayName("isValidBid: auction FINISHED → false")
        void isValidBid_auctionFinished_returnsFalse() {
            Auction auction = createRunningAuction(1000);
            auction.setStatus(AuctionStatus.FINISHED);
            assertFalse(strategy.isValidBid(auction, 1500));
        }
    }

    // ==================== AutoBidStrategy ====================

    @Nested
    @DisplayName("AutoBidStrategy")
    class AutoBidStrategyTest {

        @Test
        @DisplayName("calculateBid: auto tính currentPrice + increment")
        void calculateBid_calculatesCorrectAmount() {
            AutoBid config = new AutoBid("a1", "b1", 5000, 500);
            BidStrategy strategy = new AutoBidStrategy(config);
            Auction auction = createRunningAuction(1000);

            BidTransaction bid = strategy.calculateBid(auction, "b1", 0);

            assertEquals(1500, bid.getBidAmount()); // 1000 + 500
            assertEquals("b1", bid.getBidderId());
        }

        @Test
        @DisplayName("calculateBid: newBid > maxBid → exception")
        void calculateBid_exceedsMaxBid_throwsException() {
            AutoBid config = new AutoBid("a1", "b1", 1200, 500);
            BidStrategy strategy = new AutoBidStrategy(config);
            Auction auction = createRunningAuction(1000);
            // 1000 + 500 = 1500 > maxBid 1200

            assertThrows(IllegalArgumentException.class,
                    () -> strategy.calculateBid(auction, "b1", 0));
        }

        @Test
        @DisplayName("calculateBid: auto-bid inactive → exception")
        void calculateBid_inactiveAutoBid_throwsException() {
            AutoBid config = new AutoBid("a1", "b1", 5000, 500);
            config.setActive(false);
            BidStrategy strategy = new AutoBidStrategy(config);
            Auction auction = createRunningAuction(1000);

            assertThrows(IllegalArgumentException.class,
                    () -> strategy.calculateBid(auction, "b1", 0));
        }

        @Test
        @DisplayName("constructor: null config → exception")
        void constructor_nullConfig_throwsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new AutoBidStrategy(null));
        }

        @Test
        @DisplayName("isValidBid: đúng điều kiện → true")
        void isValidBid_validConditions_returnsTrue() {
            AutoBid config = new AutoBid("a1", "b1", 5000, 500);
            BidStrategy strategy = new AutoBidStrategy(config);
            Auction auction = createRunningAuction(1000);

            assertTrue(strategy.isValidBid(auction, 1500));
        }

        @Test
        @DisplayName("isValidBid: bidAmount > maxBid → false")
        void isValidBid_exceedsMax_returnsFalse() {
            AutoBid config = new AutoBid("a1", "b1", 1200, 500);
            BidStrategy strategy = new AutoBidStrategy(config);
            Auction auction = createRunningAuction(1000);

            assertFalse(strategy.isValidBid(auction, 1500));
        }

        @Test
        @DisplayName("isValidBid: auto-bid inactive → false")
        void isValidBid_inactive_returnsFalse() {
            AutoBid config = new AutoBid("a1", "b1", 5000, 500);
            config.setActive(false);
            BidStrategy strategy = new AutoBidStrategy(config);
            Auction auction = createRunningAuction(1000);

            assertFalse(strategy.isValidBid(auction, 1500));
        }
    }

    // ==================== Helper ====================

    private static Auction createRunningAuction(double startPrice) {
        Auction auction = new Auction("item-1", "seller-1", "Test",
                startPrice, LocalDateTime.now(),
                LocalDateTime.now().plusHours(1));
        auction.setStatus(AuctionStatus.RUNNING);
        return auction;
    }
}
