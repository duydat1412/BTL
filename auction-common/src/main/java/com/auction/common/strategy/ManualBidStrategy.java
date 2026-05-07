package com.auction.common.strategy;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;

/**
 * Chiến lược đặt giá thủ công — người dùng tự nhập số tiền.
 *
 * <p>Đây là cách đặt giá mặc định. Bidder nhập trực tiếp số tiền
 * muốn đặt, hệ thống kiểm tra hợp lệ rồi tạo BidTransaction.
 *
 * <p>Điều kiện hợp lệ:
 * <ul>
 *   <li>Phiên đấu giá phải đang {@code RUNNING}.</li>
 *   <li>Số tiền phải lớn hơn giá hiện tại ({@code currentPrice}).</li>
 * </ul>
 *
 * @see BidStrategy
 */
public class ManualBidStrategy implements BidStrategy {

    /**
     * Tạo BidTransaction với số tiền do user nhập.
     *
     * @param auction         phiên đấu giá hiện tại
     * @param bidderId        ID người đặt giá
     * @param requestedAmount số tiền user muốn đặt
     * @return BidTransaction mới
     * @throws IllegalArgumentException nếu bid không hợp lệ
     */
    @Override
    public BidTransaction calculateBid(Auction auction, String bidderId,
                                       double requestedAmount) {
        if (!isValidBid(auction, requestedAmount)) {
            throw new IllegalArgumentException(
                    "Bid không hợp lệ: giá phải > " + auction.getCurrentPrice()
                            + " và phiên đấu giá phải đang RUNNING.");
        }
        return new BidTransaction(auction.getId(), bidderId, requestedAmount);
    }

    /**
     * Kiểm tra bid thủ công có hợp lệ không.
     *
     * @param auction   phiên đấu giá
     * @param bidAmount số tiền muốn đặt
     * @return true nếu auction đang RUNNING và bidAmount &gt; currentPrice
     */
    @Override
    public boolean isValidBid(Auction auction, double bidAmount) {
        return auction.getStatus() == AuctionStatus.RUNNING
                && bidAmount > auction.getCurrentPrice();
    }
}
