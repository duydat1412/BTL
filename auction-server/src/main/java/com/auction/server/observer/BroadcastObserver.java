package com.auction.server.observer;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.entity.User;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.message.ServerPushMessage;
import com.auction.common.observer.AuctionObserver;
import com.auction.server.handler.ClientRegistry;
import com.auction.server.repository.SerializableUserRepository;

/**
 * Pushes auction events to all connected clients through the registry.
 */
public class BroadcastObserver implements AuctionObserver {

    @Override
    public void onNewBid(Auction auction, BidTransaction bid) {
        if (auction == null || bid == null) return;
        ServerPushMessage bidPush = new ServerPushMessage(
                ServerPushMessage.PushType.NEW_BID,
                "New bid placed on auction: " + auction.getTitle(),
                auction
        );
        ClientRegistry.getInstance().broadcast(bidPush);

        ServerPushMessage pricePush = new ServerPushMessage(
                ServerPushMessage.PushType.PRICE_UPDATE,
                "Current price changed for auction: " + auction.getTitle(),
                auction
        );
        ClientRegistry.getInstance().broadcast(pricePush);
    }

    @Override
    public void onAuctionStatusChanged(Auction auction,
                                       AuctionStatus oldStatus,
                                       AuctionStatus newStatus) {
        if (auction == null || newStatus == null) return;
        if (newStatus == AuctionStatus.RUNNING && oldStatus != AuctionStatus.RUNNING) {
            ServerPushMessage push = new ServerPushMessage(
                    ServerPushMessage.PushType.AUCTION_STARTED,
                    "Auction started: " + auction.getTitle(),
                    auction
            );
            ClientRegistry.getInstance().broadcast(push);
        }
    }

    @Override
    public void onAuctionEnded(Auction auction) {
        if (auction == null) return;

        String message;
        if (auction.getHighestBidderId() != null) {
            SerializableUserRepository userRepo = new SerializableUserRepository();
            User winner = userRepo.findById(auction.getHighestBidderId());
            String winnerName = (winner != null) ? winner.getUsername() : auction.getHighestBidderId();
            message = String.format("Phiên \"%s\" đã kết thúc! Người thắng: %s với giá %,.0f VNĐ",
                    auction.getTitle(), winnerName, auction.getCurrentPrice());
        } else {
            message = "Phiên \"" + auction.getTitle() + "\" đã kết thúc! (Không có người đặt giá)";
        }

        ServerPushMessage push = new ServerPushMessage(
                ServerPushMessage.PushType.AUCTION_ENDED,
                message,
                auction
        );
        ClientRegistry.getInstance().broadcast(push);
    }
}
