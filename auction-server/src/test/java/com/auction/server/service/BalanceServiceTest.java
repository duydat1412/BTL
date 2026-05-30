package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.entity.Bidder;
import com.auction.common.entity.Seller;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.datastore.DataStore;
import com.auction.server.repository.SerializableUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BalanceServiceTest {

    private final SerializableUserRepository userRepository = new SerializableUserRepository();

    @BeforeEach
    void setUp() {
        DataStore.getInstance().getUsers().clear();
        DataStore.getInstance().getAuctions().clear();
        DataStore.getInstance().getItems().clear();
        DataStore.getInstance().getBidTransactions().clear();
    }

    @AfterEach
    void tearDown() {
        DataStore.getInstance().getUsers().clear();
        DataStore.getInstance().getAuctions().clear();
        DataStore.getInstance().getItems().clear();
        DataStore.getInstance().getBidTransactions().clear();
    }

    @Test
    @DisplayName("Ignore null or unfinished auctions")
    void processAuctionEnded_ignoresInvalidAuctionState() {
        assertDoesNotThrow(() -> BalanceService.processAuctionEnded(null));

        Bidder winner = makeBidder("winner", 50_000);
        Seller seller = makeSeller("seller", 10_000);

        Auction auction = makeFinishedAuction(winner.getId(), seller.getId(), 15_000);
        auction.setStatus(AuctionStatus.RUNNING);

        BalanceService.processAuctionEnded(auction);

        assertEquals(50_000, userRepository.findById(winner.getId()).getBalance());
        assertEquals(10_000, userRepository.findById(seller.getId()).getBalance());
    }

    @Test
    @DisplayName("Ignore auctions without winner or valid participants")
    void processAuctionEnded_ignoresMissingData() {
        Seller seller = makeSeller("seller", 10_000);

        Auction noWinner = makeFinishedAuction(null, seller.getId(), 15_000);
        BalanceService.processAuctionEnded(noWinner);
        assertEquals(10_000, userRepository.findById(seller.getId()).getBalance());

        Auction missingUser = makeFinishedAuction("missing-winner", seller.getId(), 15_000);
        BalanceService.processAuctionEnded(missingUser);
        assertEquals(10_000, userRepository.findById(seller.getId()).getBalance());
    }

    @Test
    @DisplayName("Do not settle when winner balance is insufficient")
    void processAuctionEnded_skipsInsufficientWinnerBalance() {
        Bidder winner = makeBidder("winner", 5_000);
        Seller seller = makeSeller("seller", 10_000);

        Auction auction = makeFinishedAuction(winner.getId(), seller.getId(), 15_000);
        BalanceService.processAuctionEnded(auction);

        assertEquals(5_000, userRepository.findById(winner.getId()).getBalance());
        assertEquals(10_000, userRepository.findById(seller.getId()).getBalance());
    }

    @Test
    @DisplayName("Settle finished auction by debiting winner and crediting seller")
    void processAuctionEnded_transfersBalance() {
        Bidder winner = makeBidder("winner", 50_000);
        Seller seller = makeSeller("seller", 10_000);

        Auction auction = makeFinishedAuction(winner.getId(), seller.getId(), 15_000);
        BalanceService.processAuctionEnded(auction);

        assertEquals(35_000, userRepository.findById(winner.getId()).getBalance());
        assertEquals(25_000, userRepository.findById(seller.getId()).getBalance());
    }

    private Bidder makeBidder(String username, double balance) {
        Bidder bidder = new Bidder(username, "Pass@123", username + "@test.com");
        bidder.setBalance(balance);
        userRepository.save(bidder);
        return bidder;
    }

    private Seller makeSeller(String username, double balance) {
        Seller seller = new Seller(username, "Pass@123", username + "@test.com");
        seller.setBalance(balance);
        userRepository.save(seller);
        return seller;
    }

    private Auction makeFinishedAuction(String winnerId, String sellerId, double price) {
        Auction auction = new Auction("item-1", sellerId, "Auction", price,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().minusMinutes(5));
        auction.setStatus(AuctionStatus.FINISHED);
        auction.setCurrentPrice(price);
        auction.setHighestBidderId(winnerId);
        return auction;
    }
}
