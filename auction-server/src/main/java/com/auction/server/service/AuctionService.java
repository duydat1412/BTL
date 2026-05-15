package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.entity.Item;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.message.*;
import com.auction.server.repository.SerializableAuctionRepository;
import com.auction.server.repository.SerializableItemRepository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Xử lý nghiệp vụ cho phiên đấu giá (Auction).
 * Cung cấp khả năng tạo mới, truy vấn danh sách, và lấy chi tiết phiên đấu giá.
 */
public class AuctionService {

    private static final SerializableAuctionRepository auctionRepo = new SerializableAuctionRepository();
    private static final SerializableItemRepository itemRepo = new SerializableItemRepository();

    // ==================== CREATE ====================

    /**
     * Tạo phiên đấu giá mới cho 1 Item đã tồn tại.
     * Kiểm tra: item phải tồn tại, item chưa có phiên đấu giá khác đang OPEN/RUNNING.
     */
    public static ClientResponse createAuction(CreateAuctionRequest req) {
        try {
            // 1. Kiểm tra Item có tồn tại không
            Item item = itemRepo.findById(req.getItemId());
            if (item == null) {
                return new ClientResponse(false, "Sản phẩm không tồn tại!", null);
            }

            // 2. Kiểm tra xem Item này đã có phiên đấu giá đang chạy chưa
            List<Auction> allAuctions = auctionRepo.findAll();
            boolean alreadyActive = allAuctions.stream()
                    .anyMatch(a -> a.getItemId() != null
                            && a.getItemId().equals(item.getId())
                            && (a.getStatus() == AuctionStatus.OPEN || a.getStatus() == AuctionStatus.RUNNING));
            if (alreadyActive) {
                return new ClientResponse(false,
                        "Sản phẩm này đã có phiên đấu giá đang hoạt động!", null);
            }

            // 3. Tạo phiên đấu giá mới
            LocalDateTime startTime = req.getStartTime() != null
                    ? req.getStartTime() : LocalDateTime.now();
            LocalDateTime endTime = req.getEndTime() != null
                    ? req.getEndTime() : LocalDateTime.now().plusHours(1);

            Auction auction = new Auction(
                    item.getId(),
                    item.getSellerId(),
                    "Đấu giá: " + item.getName(),
                    item.getStartingPrice(),
                    startTime,
                    endTime
            );
            auction.setStatus(AuctionStatus.RUNNING);

            auctionRepo.save(auction);

            // 4. Lên lịch tự động kết thúc
            long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
            if (durationMinutes > 0) {
                AuctionScheduler.scheduleAuctionEnd(auction.getId(), durationMinutes);
            }

            return new ClientResponse(true, "Tạo phiên đấu giá thành công!", auction);

        } catch (Exception e) {
            return new ClientResponse(false, "Lỗi khi tạo phiên đấu giá: " + e.getMessage(), null);
        }
    }

    // ==================== READ (ALL) ====================

    /**
     * Lấy danh sách phiên đấu giá. Có thể lọc theo sellerId hoặc status.
     */
    public static ClientResponse getAuctions(GetAuctionsRequest req) {
        try {
            List<Auction> auctions = auctionRepo.findAll();

            if (req != null) {
                auctions = auctions.stream()
                        .filter(a -> {
                            boolean matchSeller = (req.getSellerId() == null)
                                    || (a.getSellerId() != null && a.getSellerId().equals(req.getSellerId()));
                            boolean matchStatus = (req.getStatusFilter() == null)
                                    || a.getStatus().name().equals(req.getStatusFilter());
                            return matchSeller && matchStatus;
                        })
                        .collect(Collectors.toList());
            }

            return new ClientResponse(true,
                    "Lấy danh sách thành công (" + auctions.size() + " phiên)",
                    (Serializable) auctions);

        } catch (Exception e) {
            return new ClientResponse(false, "Lỗi: " + e.getMessage(), null);
        }
    }

    // ==================== READ (SINGLE) ====================

    /**
     * Lấy chi tiết 1 phiên đấu giá theo ID.
     */
    public static ClientResponse getAuction(String auctionId) {
        try {
            Auction auction = auctionRepo.findById(auctionId);
            if (auction == null) {
                return new ClientResponse(false,
                        "Không tìm thấy phiên đấu giá với ID: " + auctionId, null);
            }
            return new ClientResponse(true, "Thành công", auction);

        } catch (Exception e) {
            return new ClientResponse(false, "Lỗi: " + e.getMessage(), null);
        }
    }
}
