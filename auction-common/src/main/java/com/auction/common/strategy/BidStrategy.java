package com.auction.common.strategy;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;

/**
 * Strategy Pattern — định nghĩa cách đặt giá trong phiên đấu giá.
 *
 * <p>Thay vì viết if/else trong BidService để phân biệt đặt giá thủ công
 * hay tự động, ta dùng Strategy: mỗi cách đặt giá là 1 class riêng.
 *
 * <p>Khi thêm cách đặt giá mới, chỉ cần tạo class mới implement interface này,
 * không cần sửa BidService (Open/Closed Principle).
 *
 * @see ManualBidStrategy
 * @see AutoBidStrategy
 */
public interface BidStrategy {

    /**
     * Tạo giao dịch đặt giá dựa trên chiến lược.
     *
     * <p>Mỗi strategy tự quyết định số tiền bid:
     * <ul>
     *   <li>Manual: dùng {@code requestedAmount} do user nhập.</li>
     *   <li>Auto: tính {@code currentPrice + increment} từ config AutoBid.</li>
     * </ul>
     *
     * @param auction         phiên đấu giá hiện tại
     * @param bidderId        ID người đặt giá
     * @param requestedAmount số tiền user muốn đặt (Manual dùng, Auto có thể bỏ qua)
     * @return BidTransaction đã tính toán xong, sẵn sàng lưu
     */
    BidTransaction calculateBid(Auction auction, String bidderId, double requestedAmount);

    /**
     * Kiểm tra bid có hợp lệ không.
     *
     * <p>Điều kiện chung: giá phải lớn hơn giá hiện tại, phiên đấu giá phải đang RUNNING.
     * Mỗi strategy có thể thêm điều kiện riêng (ví dụ Auto: không vượt maxBid).
     *
     * @param auction   phiên đấu giá
     * @param bidAmount số tiền muốn đặt
     * @return true nếu bid hợp lệ
     */
    boolean isValidBid(Auction auction, double bidAmount);
}
