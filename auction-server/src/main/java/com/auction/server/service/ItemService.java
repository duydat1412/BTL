package com.auction.server.service;

import com.auction.common.entity.Art;
import com.auction.common.entity.Electronics;
import com.auction.common.entity.Item;
import com.auction.common.entity.Vehicle;
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
            
            // Lên lịch tự động kết thúc
            AuctionScheduler.scheduleAuctionEnd(auction.getId(), durationMins);

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
        if (deleteItemRequest.getSellerId()!=senderId){
            return new ClientResponse(false, "No Permission", null);
        }
        try{
            sir.delete(deleteItemRequest.getItemId());
            return new ClientResponse(true, "Thanh cong", null);
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

