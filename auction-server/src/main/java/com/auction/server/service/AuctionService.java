package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.entity.Item;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.message.ClientResponse;
import com.auction.common.message.CreateAuctionRequest;
import com.auction.common.message.GetAuctionsRequest;
import com.auction.server.repository.SerializableAuctionRepository;
import com.auction.server.repository.SerializableItemRepository;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles auction creation and query flows.
 */
public final class AuctionService {

    private static final SerializableAuctionRepository AUCTION_REPOSITORY =
            new SerializableAuctionRepository();
    private static final SerializableItemRepository ITEM_REPOSITORY =
            new SerializableItemRepository();

    private AuctionService() {
    }

    public static ClientResponse createAuction(CreateAuctionRequest request) {
        try {
            if (request == null || request.getItemId() == null || request.getItemId().trim().isEmpty()) {
                return new ClientResponse(false, "Item id is required", null);
            }

            Item item = ITEM_REPOSITORY.findById(request.getItemId());
            if (item == null) {
                return new ClientResponse(false, "Item not found", null);
            }

            boolean alreadyActive = AUCTION_REPOSITORY.findAll().stream()
                    .anyMatch(auction -> auction.getItemId() != null
                            && auction.getItemId().equals(item.getId())
                            && (auction.getStatus() == AuctionStatus.OPEN
                            || auction.getStatus() == AuctionStatus.RUNNING));
            if (alreadyActive) {
                return new ClientResponse(false, "Item already has an active auction", null);
            }

            LocalDateTime startTime = request.getStartTime() != null
                    ? request.getStartTime() : LocalDateTime.now();
            LocalDateTime endTime = request.getEndTime() != null
                    ? request.getEndTime() : startTime.plusHours(1);
            if (!endTime.isAfter(startTime)) {
                return new ClientResponse(false, "End time must be after start time", null);
            }

            Auction auction = new Auction(
                    item.getId(),
                    item.getSellerId(),
                    "Auction: " + item.getName(),
                    item.getStartingPrice(),
                    startTime,
                    endTime
            );

            auction.setStatus(AuctionStatus.OPEN);
            AUCTION_REPOSITORY.save(auction);
            AuctionScheduler.scheduleAuctionStart(auction.getId(), startTime);

            AuctionScheduler.scheduleAuctionEndAt(auction.getId(), endTime);

            return new ClientResponse(true, "Auction created successfully", auction);
        } catch (Exception e) {
            return new ClientResponse(false, "Failed to create auction: " + e.getMessage(), null);
        }
    }

    public static ClientResponse getAuctions(GetAuctionsRequest request) {
        try {
            List<Auction> auctions = AUCTION_REPOSITORY.findAll();
            if (request != null) {
                auctions = auctions.stream()
                        .filter(auction -> {
                            boolean matchesSeller = request.getSellerId() == null
                                    || (auction.getSellerId() != null
                                    && auction.getSellerId().equals(request.getSellerId()));
                            boolean matchesStatus = request.getStatusFilter() == null
                                    || auction.getStatus().name().equals(request.getStatusFilter());
                            return matchesSeller && matchesStatus;
                        })
                        .collect(Collectors.toList());
            }

            return new ClientResponse(
                    true,
                    "Fetched " + auctions.size() + " auction(s)",
                    (Serializable) auctions
            );
        } catch (Exception e) {
            return new ClientResponse(false, "Failed to fetch auctions: " + e.getMessage(), null);
        }
    }

    public static ClientResponse getAuction(String auctionId) {
        try {
            Auction auction = AUCTION_REPOSITORY.findById(auctionId);
            if (auction == null) {
                return new ClientResponse(false, "Auction not found: " + auctionId, null);
            }
            return new ClientResponse(true, "Fetched auction successfully", auction);
        } catch (Exception e) {
            return new ClientResponse(false, "Failed to fetch auction: " + e.getMessage(), null);
        }
    }
}
