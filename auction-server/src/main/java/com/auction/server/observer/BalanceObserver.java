package com.auction.server.observer;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.observer.AuctionObserver;
import com.auction.server.service.BalanceService;

public class BalanceObserver implements AuctionObserver {

    @Override
    public void onNewBid(Auction auction, BidTransaction bid) {
        // Khong can xu ly
    }

    @Override
    public void onAuctionStatusChanged(Auction auction, AuctionStatus oldStatus, AuctionStatus newStatus) {
        // Khong can xu ly
    }

    @Override
    public void onAuctionEnded(Auction auction) {
        BalanceService.processAuctionEnded(auction);
    }
}
