package com.auction.server.observer;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.message.ServerPushMessage;
import com.auction.common.observer.AuctionObserver;
import com.auction.server.handler.ClientRegistry;

/**
 * Pushes auction events to all connected clients through the registry.
 */
public class BroadcastObserver implements AuctionObserver {

    @Override
    public void onNewBid(Auction auction, BidTransaction bid) {
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
        ServerPushMessage push = new ServerPushMessage(
                ServerPushMessage.PushType.AUCTION_ENDED,
                "Auction ended: " + auction.getTitle(),
                auction
        );
        ClientRegistry.getInstance().broadcast(push);
    }
}
