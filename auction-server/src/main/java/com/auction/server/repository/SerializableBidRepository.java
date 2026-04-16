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
        // stub: sau này sẽ filter theo auctionId khi có thuộc tính đó trong BidTransaction
        return DataStore.getInstance().getBidTransactions();
    }
}
