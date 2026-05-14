package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.repository.SerializableAuctionRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Lên lịch tự động kết thúc phiên đấu giá sau khoảng thời gian đã định.
 * Sử dụng ScheduledExecutorService để chạy task trên thread pool riêng.
 */
public class AuctionScheduler {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private static final SerializableAuctionRepository auctionRepo = new SerializableAuctionRepository();

    /**
     * Lên lịch kết thúc phiên đấu giá sau durationMinutes phút.
     */
    public static void scheduleAuctionEnd(String auctionId, long durationMinutes) {
        scheduler.schedule(() -> {
            try {
                Auction auction = auctionRepo.findById(auctionId);
                if (auction != null && auction.getStatus() == AuctionStatus.RUNNING) {
                    auction.setStatus(AuctionStatus.FINISHED);
                    auctionRepo.update(auction);
                    System.out.println("[Scheduler] Phiên đấu giá " + auctionId + " đã tự động kết thúc!");
                }
            } catch (Exception e) {
                System.err.println("[Scheduler] Lỗi khi kết thúc phiên: " + e.getMessage());
            }
        }, durationMinutes, TimeUnit.MINUTES);
        
        System.out.println("[Scheduler] Đã lên lịch kết thúc phiên " + auctionId + " sau " + durationMinutes + " phút.");
    }
}
