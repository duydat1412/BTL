package com.auction.server.repository;

import com.auction.common.entity.BidTransaction;
import com.auction.server.datastore.DataStore;
import java.util.List;

public class SerializableBidRepository implements BidRepository {
    @Override
    public void save(BidTransaction bid) {
        DataStore.getInstance().getBidTransactions().add(bid);
        DataStore.getInstance().saveData();
    }

    @Override
    public List<BidTransaction> findByAuctionId(String auctionId) {
        return DataStore.getInstance().getBidTransactions().stream()
                .filter(b -> b.getAuctionId() != null && b.getAuctionId().equals(auctionId))
                .collect(java.util.stream.Collectors.toList());
    }
}
