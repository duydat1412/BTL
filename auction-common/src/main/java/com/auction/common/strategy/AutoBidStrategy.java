package com.auction.common.strategy;

import com.auction.common.entity.AutoBid;
import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;

/**
 * Chiến lược đặt giá tự động — hệ thống tự tăng giá dựa trên config AutoBid.
 *
 * <p>Khi có bid mới từ người khác, hệ thống tự động đặt giá:
 * {@code newBid = currentPrice + increment}, miễn là không vượt {@code maxBid}.
 *
 * <p>Điều kiện hợp lệ:
 * <ul>
 *   <li>Phiên đấu giá phải đang {@code RUNNING}.</li>
 *   <li>{@code currentPrice + increment <= maxBid}.</li>
 *   <li>AutoBid config phải đang {@code active}.</li>
 * </ul>
 *
 * <p>Logic chi tiết sẽ được Người D triển khai trong AutoBidService (Tuần 13-14).
 * Class này chỉ lo phần tính toán giá bid.
 *
 * @see BidStrategy
 * @see AutoBid
 */
public class AutoBidStrategy implements BidStrategy {

    /** Cấu hình auto-bid (maxBid, increment). */
    private final AutoBid autoBidConfig;

    /**
     * Constructor — nhận config AutoBid.
     *
     * @param autoBidConfig cấu hình auto-bid của bidder
     * @throws IllegalArgumentException nếu config null
     */
    public AutoBidStrategy(AutoBid autoBidConfig) {
        if (autoBidConfig == null) {
            throw new IllegalArgumentException("AutoBid config không được null.");
        }
        this.autoBidConfig = autoBidConfig;
    }

    /**
     * Tính giá bid tự động: {@code currentPrice + increment}.
     *
     * <p>Tham số {@code requestedAmount} bị bỏ qua vì Auto tự tính.
     *
     * @param auction         phiên đấu giá hiện tại
     * @param bidderId        ID người đặt giá
     * @param requestedAmount không sử dụng (Auto tự tính)
     * @return BidTransaction với giá đã tính
     * @throws IllegalArgumentException nếu bid không hợp lệ
     */
    @Override
    public BidTransaction calculateBid(Auction auction, String bidderId,
                                       double requestedAmount) {
        double newBid = auction.getCurrentPrice() + autoBidConfig.getIncrement();

        if (!isValidBid(auction, newBid)) {
            throw new IllegalArgumentException(
                    "Auto-bid không hợp lệ: giá " + newBid
                            + " vượt maxBid " + autoBidConfig.getMaxBid()
                            + " hoặc phiên đấu giá không đang RUNNING.");
        }
        return new BidTransaction(auction.getId(), bidderId, newBid);
    }

    /**
     * Kiểm tra auto-bid có hợp lệ không.
     *
     * @param auction   phiên đấu giá
     * @param bidAmount số tiền muốn đặt (đã tính = currentPrice + increment)
     * @return true nếu auction đang RUNNING, auto-bid đang active, và giá &le; maxBid
     */
    @Override
    public boolean isValidBid(Auction auction, double bidAmount) {
        return auction.getStatus() == AuctionStatus.RUNNING
                && autoBidConfig.isActive()
                && bidAmount > auction.getCurrentPrice()
                && bidAmount <= autoBidConfig.getMaxBid();
    }
}
