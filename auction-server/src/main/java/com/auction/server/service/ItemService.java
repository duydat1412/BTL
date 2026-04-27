package com.auction.server.service;

import com.auction.common.entity.Art;
import com.auction.common.entity.Electronics;
import com.auction.common.entity.Item;
import com.auction.common.entity.Vehicle;
import com.auction.common.factory.ItemFactory;
import com.auction.common.message.ClientResponse;
import com.auction.common.message.CreateItemRequest;
import com.auction.common.message.GetItemsRequest;
import com.auction.server.repository.SerializableItemRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Xử lý nghiệp vụ CRUD cho sản phẩm (Item).
 */
public class ItemService {

    private static final SerializableItemRepository itemRepo = new SerializableItemRepository();

    // ==================== CREATE ====================

    /**
     * Tạo sản phẩm mới từ CreateItemRequest.
     * Dùng ItemFactory (Factory Method Pattern) để khởi tạo đúng loại subclass.
     */
    public static ClientResponse createItem(CreateItemRequest request) {
        try {
            // --- Validation ---
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

            // Dùng Factory Method Pattern (Người B đã viết) để tạo đúng subclass
            Item item = ItemFactory.createItem(request.getItemType());

            // Set thông tin chung
            item.setName(request.getName());
            item.setDescription(request.getDescription());
            item.setStartingPrice(request.getStartingPrice());
            item.setSellerId(request.getSellerId());

            // Set thông tin riêng từ extraAttributes
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

            itemRepo.save(item);
            return new ClientResponse(true, "Tạo sản phẩm thành công", item);

        } catch (Exception e) {
            return new ClientResponse(false, "Lỗi khi tạo sản phẩm: " + e.getMessage(), null);
        }
    }

    // ==================== READ ====================

    /**
     * Lấy danh sách sản phẩm, có thể lọc theo sellerId và/hoặc itemType.
     * Dùng findAll() + Stream filter (KHÔNG dùng findById vì đây là lấy danh sách).
     */
    public static ClientResponse getItems(GetItemsRequest request) {
        try {
            List<Item> allItems = itemRepo.findAll();

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

    // ==================== UPDATE ====================

    public void U() {
    }

    // ==================== DELETE ====================

    public void D() {
    }

    // ==================== Helper ====================

    /**
     * Parse String sang int an toàn, trả về 0 nếu không hợp lệ.
     */
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
