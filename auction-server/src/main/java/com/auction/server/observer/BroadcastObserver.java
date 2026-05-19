package com.auction.server.observer;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.message.ServerPushMessage;
import com.auction.common.observer.AuctionObserver;
import com.auction.server.handler.ClientRegistry;

/**
 * Observer thực thi push notification xuống tất cả Client qua ClientRegistry.
 *
 * <p>Khi BidService gọi eventManager.notifyNewBid(), observer này sẽ tạo
 * ServerPushMessage rồi broadcast qua tất cả Socket đang kết nối.
 */
public class BroadcastObserver implements AuctionObserver {

    @Override
    public void onNewBid(Auction auction, BidTransaction bid) {
        ServerPushMessage push = new ServerPushMessage(
                ServerPushMessage.PushType.NEW_BID,
                "Có người vừa đặt giá " + String.format("%,.0f", bid.getBidAmount())
                        + " cho phiên: " + auction.getTitle(),
                auction
        );
        ClientRegistry.getInstance().broadcast(push);
    }

    @Override
    public void onAuctionStatusChanged(Auction auction,
                                        AuctionStatus oldStatus, AuctionStatus newStatus) {
        // Có thể push thông báo khi trạng thái thay đổi
    }

    @Override
    public void onAuctionEnded(Auction auction) {
        ServerPushMessage push = new ServerPushMessage(
                ServerPushMessage.PushType.AUCTION_ENDED,
                "Phiên đấu giá '" + auction.getTitle() + "' đã kết thúc!",
                auction
        );
        ClientRegistry.getInstance().broadcast(push);
    }
}
