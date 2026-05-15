package com.auction.common.message;

import java.io.Serializable;

/**
 * Tin nhắn Server tự động đẩy xuống Client (không cần Client hỏi).
 * Dùng để thông báo real-time: bid mới, auction kết thúc, v.v.
 */
public class ServerPushMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Loại sự kiện được push. */
    private final PushType type;

    /** Dữ liệu kèm theo (VD: Auction, BidTransaction). */
    private final Serializable data;

    /** Tin nhắn mô tả. */
    private final String message;

    public ServerPushMessage(PushType type, String message, Serializable data) {
        this.type = type;
        this.message = message;
        this.data = data;
    }

    public PushType getType() {
        return type;
    }

    public Serializable getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Các loại sự kiện mà Server có thể push.
     */
    public enum PushType {
        NEW_BID,
        AUCTION_ENDED,
        AUCTION_STARTED
    }
}
