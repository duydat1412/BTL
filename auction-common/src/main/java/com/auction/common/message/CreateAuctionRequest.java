package com.auction.common.message;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Payload cho lệnh CREATE_AUCTION.
 * Client gửi kèm itemId và thời gian kết thúc mong muốn.
 */
public class CreateAuctionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String itemId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public CreateAuctionRequest(String itemId, LocalDateTime startTime, LocalDateTime endTime) {
        this.itemId = itemId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getItemId() {
        return itemId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
