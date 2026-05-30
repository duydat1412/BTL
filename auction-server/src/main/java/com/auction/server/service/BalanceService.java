package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.entity.User;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.repository.SerializableUserRepository;

import java.util.concurrent.locks.ReentrantLock;

public final class BalanceService {

    private static final SerializableUserRepository USER_REPOSITORY = new SerializableUserRepository();

    private BalanceService() {}

    public static void processAuctionEnded(Auction auction) {
        if (auction == null || auction.getStatus() != AuctionStatus.FINISHED) return;
        if (auction.getHighestBidderId() == null) return;

        String winnerId = auction.getHighestBidderId();
        String sellerId = auction.getSellerId();
        double amount = auction.getCurrentPrice();

        if (winnerId == null || sellerId == null || amount <= 0) return;

        User winner = USER_REPOSITORY.findById(winnerId);
        User seller = USER_REPOSITORY.findById(sellerId);

        if (winner == null || seller == null) return;

        // Tru lock theo tung user de tranh race condition khi 2 auction finish dong thoi
        synchronized (winnerId.intern()) {
            if (winner.getBalance() < amount) {
                System.out.println("[Balance] Warning: winner " + winner.getUsername()
                        + " has insufficient balance (" + String.format("%,.0f", winner.getBalance())
                        + ") for auction " + auction.getId() + " (" + String.format("%,.0f", amount) + ")");
                return;
            }
            winner.setBalance(winner.getBalance() - amount);
            USER_REPOSITORY.update(winner);
        }

        synchronized (sellerId.intern()) {
            seller.setBalance(seller.getBalance() + amount);
            USER_REPOSITORY.update(seller);
        }

        System.out.println("[Balance] Auction " + auction.getId()
                + " settled: -" + String.format("%,.0f", amount) + " from " + winner.getUsername()
                + " (winner), +" + String.format("%,.0f", amount) + " to " + seller.getUsername()
                + " (seller)");
    }
}
