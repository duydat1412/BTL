package com.auction.server.service;

import com.auction.common.entity.Art;
import com.auction.common.entity.Electronics;
import com.auction.common.entity.Item;
import com.auction.common.entity.Vehicle;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.factory.ItemFactory;
import com.auction.common.message.*;
import com.auction.server.repository.SerializableItemRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Xử lý nghiệp vụ CRUD cho sản phẩm (Item).
 */
public class ItemService {

    private static final SerializableItemRepository sir = new SerializableItemRepository();

    // ==================== CREATE ====================

    public static ClientResponse C(CreateItemRequest request) {
        try {
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return new ClientResponse(false, "Tên sản phẩm không được để trống", null);
            }
            if (request.getStartingPrice() <= 0) {
                return new ClientResponse(false, "Giá khởi điểm phải lớn hơn 0", null);
            }
            if (request.getSellerId() == null) {
                return new ClientResponse(false, "Thiếu thông tin người bán", null);
            }
            if (request.getItemType() == null) {
                return new ClientResponse(false, "Phải chọn loại sản phẩm", null);
            }

            Item item = ItemFactory.createItem(request.getItemType());
            item.setName(request.getName());
            item.setDescription(request.getDescription());
            item.setStartingPrice(request.getStartingPrice());
            item.setSellerId(request.getSellerId());

            Map<String, String> attr = request.getExtraAttributes();
            if (attr != null) {
                switch (request.getItemType()) {
                    case ELECTRONICS -> {
                        Electronics e = (Electronics) item;
                        e.setBrand(attr.getOrDefault("brand", ""));
                        e.setModel(attr.getOrDefault("model", ""));
                        e.setWarrantyMonths(parseIntSafe(attr.get("warrantyMonths")));
                    }
                    case ART -> {
                        Art a = (Art) item;
                        a.setArtist(attr.getOrDefault("artist", ""));
                        a.setMedium(attr.getOrDefault("medium", ""));
                        a.setYear(parseIntSafe(attr.get("year")));
                    }
                    case VEHICLE -> {
                        Vehicle v = (Vehicle) item;
                        v.setManufacturer(attr.getOrDefault("manufacturer", ""));
                        v.setYearOfManufacture(parseIntSafe(attr.get("yearOfManufacture")));
                        v.setMileage(parseIntSafe(attr.get("mileage")));
                    }
                }
            }

            sir.save(item);
            
            // Tự động tạo Auction cho Item này
            com.auction.common.entity.Auction auction = new com.auction.common.entity.Auction();
            auction.setItemId(item.getId());
            auction.setSellerId(item.getSellerId());
            auction.setTitle(item.getName() + " - Phiên Đấu Giá");
            auction.setStartPrice(item.getStartingPrice());
            auction.setCurrentPrice(item.getStartingPrice());
            auction.setStatus(com.auction.common.enums.AuctionStatus.OPEN);
            
            long durationMins = 60; // default 60 minutes
            if (attr != null && attr.containsKey("durationMinutes")) {
                try {
                    durationMins = Long.parseLong(attr.get("durationMinutes"));
                } catch (Exception ignored) {}
            }
            
            auction.setStartTime(java.time.LocalDateTime.now());
            auction.setEndTime(java.time.LocalDateTime.now().plusMinutes(durationMins));
            
            com.auction.server.repository.SerializableAuctionRepository auctionRepo = new com.auction.server.repository.SerializableAuctionRepository();
            auctionRepo.save(auction);
            System.out.println("[DEBUG] ItemService: created auction " + auction.getId() + " for item " + item.getId());
            
            // Lên lịch tự động kết thúc
            AuctionScheduler.scheduleAuctionStart(auction.getId(), auction.getStartTime());
            AuctionScheduler.scheduleAuctionEndAt(auction.getId(), auction.getEndTime());

            // Broadcast thông báo auction mới cho tất cả client
            try {
                com.auction.common.message.ServerPushMessage pushMsg = new com.auction.common.message.ServerPushMessage(
                        com.auction.common.message.ServerPushMessage.PushType.AUCTION_CREATED,
                        "Phiên đấu giá mới: " + auction.getTitle(),
                        auction
                );
                com.auction.server.handler.ClientRegistry.getInstance().broadcast(pushMsg);
            } catch (Exception pushEx) {
                System.err.println("[WARN] Failed to broadcast AUCTION_CREATED: " + pushEx.getMessage());
            }

            return new ClientResponse(true, "Tạo sản phẩm và phiên đấu giá thành công", item);

        } catch (Exception e) {
            return new ClientResponse(false, "Lỗi khi tạo sản phẩm: " + e.getMessage(), null);
        }
    }

    // ==================== READ ====================

    public static ClientResponse R(GetItemsRequest request) {
        try {
            List<Item> allItems = sir.findAll();

            List<Item> filtered = allItems.stream()
                    .filter(item -> {
                        boolean matchSeller = (request.getSellerId() == null)
                                || (item.getSellerId() != null
                                && item.getSellerId().equals(request.getSellerId()));
                        boolean matchType = (request.getItemType() == null)
                                || (item.getItemType() == request.getItemType());
                        return matchSeller && matchType;
                    })
                    .collect(Collectors.toList());

            return new ClientResponse(true,
                    "Lấy danh sách thành công (" + filtered.size() + " sản phẩm)",
                    (Serializable) filtered);

        } catch (Exception e) {
            return new ClientResponse(false, "Lỗi khi lấy danh sách: " + e.getMessage(), null);
        }
    }

    // ==================== UPDATE (Người D sẽ implement) ====================

    public ClientResponse U(UpdateItemRequest uir) {
        Map<String, String> attr=uir.getExtraAttributes();
        try {
            Item item = sir.findById(uir.getItemId());
            if (item==null){
                return new ClientResponse(false, "invalid item id", null);
            } else {
                if (uir.getName()!=null) {
                    item.setName(uir.getName());
                }
                if (uir.getDescription()!=null) {
                    item.setDescription(uir.getDescription());
                }
                if (uir.getStartingPrice()!=null) {
                    item.setStartingPrice(uir.getStartingPrice());
                }
                if (uir.getItemType()!=null) {
                    item.setItemType(uir.getItemType());
                }
                if (uir.getExtraAttributes()!=null) {
                    switch (item.getItemType()) {
                        case ELECTRONICS:
                            Electronics e = (Electronics) item;
                            if (attr.containsKey("brand")) {
                                e.setBrand(attr.get("brand"));
                            }
                            if (attr.containsKey("model")) {
                                e.setModel(attr.get("model"));
                            }
                            if (attr.containsKey("warrantyMonths")) {
                                e.setWarrantyMonths(parseIntSafe(attr.get("warrantyMonths")));
                            }
                            break;
                        case ART:
                            Art a = (Art) item;
                            if (attr.containsKey("artist")) {
                                a.setArtist(attr.get("artist"));
                            }
                            if (attr.containsKey("medium")) {
                                a.setMedium(attr.get("medium"));
                            }
                            if (attr.containsKey("year")) {
                                a.setYear(parseIntSafe(attr.get("year")));
                            }
                            break;
                        case VEHICLE:
                            Vehicle v = (Vehicle) item;
                            if (attr.containsKey("manufacturer")) {
                                v.setManufacturer(attr.get("manufacturer"));
                            }
                            if (attr.containsKey("yearOfManufacture")) {
                                v.setYearOfManufacture(parseIntSafe(attr.get("yearOfManufacture")));
                            }
                            if (attr.containsKey("mileage")) {
                                v.setMileage(parseIntSafe(attr.get("mileage")));
                            }
                            break;
                    }
                }
                sir.update();
                return new ClientResponse(true, "cap nhat thanh cong", item);
            }
        } catch (Exception e){
            return new ClientResponse(false, e.getMessage(), null);
        }
    }

    // ==================== DELETE (Người D sẽ implement) ====================

    public ClientResponse D(DeleteItemRequest deleteItemRequest, String senderId) {
        if (deleteItemRequest.getSellerId() == null || !deleteItemRequest.getSellerId().equals(senderId)){
            return new ClientResponse(false, "No Permission", null);
        }
        try{
            // Không cho xóa nếu item đã có phiên FINISHED (có người thắng)
            com.auction.server.repository.SerializableAuctionRepository auctionRepo =
                    new com.auction.server.repository.SerializableAuctionRepository();
            boolean hasFinished = auctionRepo.findAll().stream()
                    .anyMatch(a -> deleteItemRequest.getItemId().equals(a.getItemId())
                            && a.getStatus() == AuctionStatus.FINISHED);
            if (hasFinished) {
                return new ClientResponse(false, "Sản phẩm đã có phiên đấu giá kết thúc, không thể xóa", null);
            }

            // Xóa item
            sir.delete(deleteItemRequest.getItemId());

            // Xóa các auction liên kết với item này (chỉ xóa nếu chưa FINISHED)
            java.util.List<com.auction.common.entity.Auction> toRemove = auctionRepo.findAll().stream()
                    .filter(a -> deleteItemRequest.getItemId().equals(a.getItemId()))
                    .filter(a -> a.getStatus() != AuctionStatus.FINISHED)
                    .toList();
            // Xóa trước — tránh race: nếu broadcast rồi mới xóa, client loadAuctions()
            // có thể GET_AUCTIONS trước khi xóa kịp hoàn tất, nhận về auction cũ
            for (com.auction.common.entity.Auction a : toRemove) {
                auctionRepo.delete(a.getId());
            }
            // Broadcast sau khi xóa — client load lại list sẽ không thấy auction
            for (com.auction.common.entity.Auction a : toRemove) {
                try {
                    com.auction.common.message.ServerPushMessage pushMsg = new com.auction.common.message.ServerPushMessage(
                            com.auction.common.message.ServerPushMessage.PushType.AUCTION_ENDED,
                            "Phiên \"" + a.getTitle() + "\" đã bị xóa",
                            a
                    );
                    com.auction.server.handler.ClientRegistry.getInstance().broadcast(pushMsg);
                } catch (Exception pushEx) {
                    System.err.println("[WARN] Failed to broadcast auction delete: " + pushEx.getMessage());
                }
            }

            return new ClientResponse(true, "Xóa thành công", null);
        } catch (Exception e){
            return new ClientResponse(false, e.getMessage(), null);
        }
    }

    // ==================== Helper ====================

    private static int parseIntSafe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

