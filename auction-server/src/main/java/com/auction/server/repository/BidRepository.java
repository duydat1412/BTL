package com.auction.server.repository;

import com.auction.common.entity.BidTransaction;
import java.util.List;

public interface BidRepository {
    void save(BidTransaction bid);
    List<BidTransaction> findByAuctionId(String auctionId);
}
