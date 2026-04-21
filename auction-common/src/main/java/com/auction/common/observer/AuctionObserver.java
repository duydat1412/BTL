package com.auction.common.observer;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;

/**
 * Observer interface — lắng nghe sự kiện trong phiên đấu giá.
 *
 * <p>Đây là phần Observer trong Observer Pattern.
 * Các class muốn nhận thông báo khi có sự kiện đấu giá
 * (bid mới, trạng thái thay đổi, kết thúc) sẽ implement interface này.
 *
 * <p>Interface nằm trong module common để cả Server và Client đều dùng được.
 * Server-side: {@code AuctionEventManager} quản lý danh sách observer.
 * Client-side: Controller có thể implement để cập nhật UI realtime.
 *
 * @see com.auction.common.entity.Auction
 * @see com.auction.common.entity.BidTransaction
 */
public interface AuctionObserver {

    /**
     * Được gọi khi có bid mới trong phiên đấu giá.
     *
     * @param auction phiên đấu giá (đã cập nhật giá mới)
     * @param bid     giao dịch bid vừa xảy ra
     */
    void onNewBid(Auction auction, BidTransaction bid);

    /**
     * Được gọi khi trạng thái phiên đấu giá thay đổi.
     * Ví dụ: OPEN → RUNNING, RUNNING → FINISHED.
     *
     * @param auction   phiên đấu giá
     * @param oldStatus trạng thái cũ
     * @param newStatus trạng thái mới
     */
    void onAuctionStatusChanged(Auction auction, AuctionStatus oldStatus, AuctionStatus newStatus);

    /**
     * Được gọi khi phiên đấu giá kết thúc.
     *
     * @param auction phiên đấu giá đã kết thúc
     */
    void onAuctionEnded(Auction auction);
}
