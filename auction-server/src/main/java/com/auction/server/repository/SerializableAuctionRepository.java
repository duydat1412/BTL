package com.auction.server.repository;

import com.auction.common.entity.Auction;
import com.auction.server.datastore.DataStore;
import java.util.List;

public class SerializableAuctionRepository implements AuctionRepository {
    @Override
    public void save(Auction auction) {
        DataStore.getInstance().getAuctions().add(auction);
        DataStore.getInstance().saveData();
    }

    @Override
    public Auction findById(String id) {
        return DataStore.getInstance().getAuctions().stream()
                .filter(a -> a.getId() != null && a.getId().equals(id))
                .findFirst().orElse(null);
    }

    @Override
    public List<Auction> findAll() { return DataStore.getInstance().getAuctions(); }

    @Override
    public void update(Auction auction) { DataStore.getInstance().saveData(); }

    @Override
    public void delete(String id) {
        DataStore.getInstance().getAuctions().removeIf(a -> a.getId() != null && a.getId().equals(id));
        DataStore.getInstance().saveData();
    }
}
