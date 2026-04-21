package com.auction.server.observer;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.observer.AuctionObserver;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject (Publisher) trong Observer Pattern — quản lý và thông báo observer.
 *
 * <p>AuctionEventManager giữ danh sách các {@link AuctionObserver} đã đăng ký.
 * Khi có sự kiện (bid mới, status thay đổi, đấu giá kết thúc),
 * gọi notify để thông báo tất cả observer.
 *
 * <p>Thread-safe: dùng {@link CopyOnWriteArrayList} để tránh
 * ConcurrentModificationException khi subscribe/unsubscribe trong lúc notify.
 *
 * <p>Là nền tảng cho realtime update qua Socket — khi Server notify,
 * các observer gửi message qua Socket tới Client để cập nhật UI.
 *
 * @see AuctionObserver
 */
public class AuctionEventManager {

    /**
     * Danh sách observer — CopyOnWriteArrayList đảm bảo thread-safe.
     * Phù hợp khi đọc (notify) nhiều hơn ghi (subscribe/unsubscribe).
     */
    private final List<AuctionObserver> observers = new CopyOnWriteArrayList<>();

    /**
     * Đăng ký observer nhận thông báo sự kiện.
     *
     * @param observer observer cần đăng ký
     */
    public void subscribe(AuctionObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Hủy đăng ký observer.
     *
     * @param observer observer cần hủy
     */
    public void unsubscribe(AuctionObserver observer) {
        observers.remove(observer);
    }

    /**
     * Thông báo tất cả observer khi có bid mới.
     *
     * @param auction phiên đấu giá (đã cập nhật giá mới)
     * @param bid     giao dịch bid vừa xảy ra
     */
    public void notifyNewBid(Auction auction, BidTransaction bid) {
        for (AuctionObserver observer : observers) {
            observer.onNewBid(auction, bid);
        }
    }

    /**
     * Thông báo tất cả observer khi trạng thái phiên đấu giá thay đổi.
     *
     * @param auction   phiên đấu giá
     * @param oldStatus trạng thái cũ
     * @param newStatus trạng thái mới
     */
    public void notifyStatusChanged(Auction auction, AuctionStatus oldStatus,
                                    AuctionStatus newStatus) {
        for (AuctionObserver observer : observers) {
            observer.onAuctionStatusChanged(auction, oldStatus, newStatus);
        }
    }

    /**
     * Thông báo tất cả observer khi phiên đấu giá kết thúc.
     *
     * @param auction phiên đấu giá đã kết thúc
     */
    public void notifyAuctionEnded(Auction auction) {
        for (AuctionObserver observer : observers) {
            observer.onAuctionEnded(auction);
        }
    }

    /**
     * Trả về số lượng observer đang đăng ký.
     *
     * @return số observer
     */
    public int getObserverCount() {
        return observers.size();
    }
}
