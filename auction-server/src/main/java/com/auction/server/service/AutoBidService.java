package com.auction.server.service;

import com.auction.common.entity.*;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.observer.AuctionObserver;
import com.auction.common.strategy.AutoBidStrategy;
import com.auction.server.exception.AuctionClosedException;
import com.auction.server.exception.InvalidBidException;
import com.auction.server.repository.AuctionRepository;
import com.auction.server.repository.BidRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AutoBidService implements AuctionObserver {

    private final Map<String, List<AutoBid>> autoBidsByAuction = new ConcurrentHashMap<>();
    private final BidService bidService;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    public AutoBidService(BidService bidService, AuctionRepository auctionRepository, BidRepository bidRepository) {
        this.bidService = bidService;
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
    }

    public void registerAutoBid(AutoBid config) {
        autoBidsByAuction
                .computeIfAbsent(config.getAuctionId(), k -> new CopyOnWriteArrayList<>())
                .add(config);
        System.out.println("[AutoBid] Registered auto-bid: " + config.getInfo());
    }

    public void removeAutoBid(String auctionId, String bidderId) {
        List<AutoBid> configs = autoBidsByAuction.get(auctionId);
        if (configs != null) {
            configs.removeIf(c -> c.getBidderId().equals(bidderId));
            if (configs.isEmpty()) {
                autoBidsByAuction.remove(auctionId);
            }
        }
        System.out.println("[AutoBid] Removed auto-bid for bidder " + bidderId + " on auction " + auctionId);
    }

    public List<AutoBid> getAutoBidsForAuction(String auctionId) {
        List<AutoBid> configs = autoBidsByAuction.get(auctionId);
        return configs != null ? Collections.unmodifiableList(configs) : Collections.emptyList();
    }

    public boolean hasAutoBid(String auctionId, String bidderId) {
        List<AutoBid> configs = autoBidsByAuction.get(auctionId);
        if (configs == null) return false;
        return configs.stream().anyMatch(c -> c.getBidderId().equals(bidderId) && c.isActive());
    }

    @Override
    public void onNewBid(Auction auction, BidTransaction bid) {
        String auctionId = auction.getId();
        List<AutoBid> configs = autoBidsByAuction.get(auctionId);
        if (configs == null || configs.isEmpty()) return;

        Auction freshAuction = auctionRepository.findById(auctionId);
        if (freshAuction == null || freshAuction.getStatus() != AuctionStatus.RUNNING) return;

        for (AutoBid config : configs) {
            if (!config.isActive()) continue;
            if (config.getBidderId().equals(bid.getBidderId())) continue;
            if (config.getMaxBid() <= freshAuction.getCurrentPrice()) continue;

            try {
                AutoBidStrategy strategy = new AutoBidStrategy(config);
                bidService.placeBid(auctionId, config.getBidderId(), 0, strategy);
                System.out.println("[AutoBid] Auto-placed bid for " + config.getBidderId()
                        + " on auction " + auctionId);
            } catch (InvalidBidException e) {
                config.setActive(false);
                System.out.println("[AutoBid] Deactivated for " + config.getBidderId()
                        + ": " + e.getMessage());
            } catch (AuctionClosedException e) {
                config.setActive(false);
                System.out.println("[AutoBid] Deactivated for " + config.getBidderId()
                        + ": auction closed");
            }
        }
    }

    @Override
    public void onAuctionStatusChanged(Auction auction, AuctionStatus oldStatus, AuctionStatus newStatus) {
        if (newStatus == AuctionStatus.FINISHED || newStatus == AuctionStatus.CANCELED) {
            List<AutoBid> configs = autoBidsByAuction.remove(auction.getId());
            if (configs != null && !configs.isEmpty()) {
                System.out.println("[AutoBid] Cleared " + configs.size()
                        + " configs for ended auction " + auction.getId());
            }
        }
    }

    @Override
    public void onAuctionEnded(Auction auction) {
        autoBidsByAuction.remove(auction.getId());
    }
}
