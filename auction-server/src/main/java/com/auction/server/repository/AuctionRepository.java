package com.auction.server.repository;

import com.auction.common.entity.Auction;
import java.util.List;

public interface AuctionRepository {
    void save(Auction auction);
    Auction findById(String id);
    List<Auction> findAll();
    void update(Auction auction);
    void delete(String id);
}
