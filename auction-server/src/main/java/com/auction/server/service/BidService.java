package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.strategy.BidStrategy;
import com.auction.server.exception.AuctionClosedException;
import com.auction.server.exception.InvalidBidException;
import com.auction.server.observer.AuctionEventManager;
import com.auction.server.repository.AuctionRepository;
import com.auction.server.repository.BidRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service xử lý đặt giá — có concurrency protection bằng ReentrantLock.
 *
 * <p>Mỗi Auction có 1 {@link ReentrantLock} riêng trong {@code auctionLocks}.
 * Khi 2+ bidder đặt giá cùng lúc trên 1 auction, lock đảm bảo
 * chỉ 1 thread xử lý tại 1 thời điểm → tránh race condition (lost update).
 *
 * <p>Các bid trên auction khác vẫn chạy song song (fine-grained locking).
 *
 * <p>Sử dụng Strategy Pattern: nhận {@link BidStrategy} để quyết định
 * cách tính giá bid (Manual hoặc Auto), tuân thủ Open/Closed Principle.
 *
 * <p>Dependency Injection: nhận {@link AuctionRepository}, {@link BidRepository},
 * {@link AuctionEventManager} qua constructor — tuân thủ Dependency Inversion Principle.
 *
 * @see BidStrategy
 * @see com.auction.common.strategy.ManualBidStrategy
 * @see com.auction.common.strategy.AutoBidStrategy
 */
public class BidService {

    /**
     * Map: auctionId → Lock riêng cho auction đó.
     * Dùng ConcurrentHashMap để thread-safe khi tạo lock mới.
     */
    private final Map<String, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();

    /** Repository để truy xuất và cập nhật Auction. */
    private final AuctionRepository auctionRepository;

    /** Repository để lưu BidTransaction. */
    private final BidRepository bidRepository;

    /** Observer manager — thông báo khi có bid mới. */
    private final AuctionEventManager eventManager;

    /**
     * Constructor — inject dependencies.
     *
     * @param auctionRepository repository quản lý Auction
     * @param bidRepository     repository quản lý BidTransaction
     * @param eventManager      observer manager thông báo sự kiện
     */
    public BidService(AuctionRepository auctionRepository,
                      BidRepository bidRepository,
                      AuctionEventManager eventManager) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.eventManager = eventManager;
    }

    /**
     * Đặt giá vào phiên đấu giá — THREAD-SAFE.
     *
     * <p>Flow xử lý:
     * <ol>
     *   <li>Lấy lock cho auction (tạo mới nếu chưa có) bằng {@code computeIfAbsent}.</li>
     *   <li>Lock lại — chỉ 1 thread xử lý tại 1 thời điểm.</li>
     *   <li>Tìm Auction từ repository, kiểm tra tồn tại.</li>
     *   <li>Kiểm tra auction đang RUNNING (nếu không → {@link AuctionClosedException}).</li>
     *   <li>Dùng {@link BidStrategy#isValidBid} để validate giá.</li>
     *   <li>Dùng {@link BidStrategy#calculateBid} để tạo {@link BidTransaction}.</li>
     *   <li>Cập nhật {@code currentPrice} và {@code highestBidderId} trên Auction.</li>
     *   <li>Lưu BidTransaction qua repository.</li>
     *   <li>Cập nhật Auction qua repository.</li>
     *   <li>Thông báo observer (bid mới).</li>
     *   <li>Unlock trong {@code finally} block (rất quan trọng!).</li>
     * </ol>
     *
     * @param auctionId ID phiên đấu giá
     * @param bidderId  ID người đặt giá
     * @param amount    số tiền muốn đặt (Manual dùng, Auto có thể bỏ qua)
     * @param strategy  chiến lược đặt giá (Manual hoặc Auto)
     * @return BidTransaction đã lưu thành công
     * @throws AuctionClosedException nếu auction không đang RUNNING
     * @throws InvalidBidException    nếu giá đặt không hợp lệ
     */
    public BidTransaction placeBid(String auctionId, String bidderId,
                                   double amount, BidStrategy strategy)
            throws AuctionClosedException, InvalidBidException {

        // Lấy hoặc tạo lock riêng cho auction này
        ReentrantLock lock = auctionLocks.computeIfAbsent(auctionId,
                k -> new ReentrantLock());
        lock.lock();
        try {
            // 1. Tìm Auction
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                throw new InvalidBidException(
                        "Không tìm thấy phiên đấu giá với ID: " + auctionId);
            }

            // 2. Kiểm tra auction đang RUNNING
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                throw new AuctionClosedException(
                        "Phiên đấu giá '" + auction.getTitle()
                                + "' không đang diễn ra. Trạng thái hiện tại: "
                                + auction.getStatus().getDisplayName());
            }

            // 3. Validate bid bằng Strategy
            if (!strategy.isValidBid(auction, amount)) {
                throw new InvalidBidException(
                        "Bid không hợp lệ: giá " + amount
                                + " phải lớn hơn giá hiện tại "
                                + auction.getCurrentPrice());
            }

            // 4. Tạo BidTransaction bằng Strategy
            BidTransaction bidTransaction = strategy.calculateBid(
                    auction, bidderId, amount);

            // 5. Cập nhật Auction
            auction.setCurrentPrice(bidTransaction.getBidAmount());
            auction.setHighestBidderId(bidderId);

            // 6. Lưu vào repository
            bidRepository.save(bidTransaction);
            auctionRepository.update(auction);

            // 7. Thông báo observer
            eventManager.notifyNewBid(auction, bidTransaction);

            return bidTransaction;

        } finally {
            // LUÔN unlock dù có exception — tránh deadlock!
            lock.unlock();
        }
    }

    /**
     * Lấy lịch sử bid của một phiên đấu giá.
     *
     * @param auctionId ID phiên đấu giá
     * @return danh sách BidTransaction
     */
    public List<BidTransaction> getBidHistory(String auctionId) {
        return bidRepository.findByAuctionId(auctionId);
    }
}
