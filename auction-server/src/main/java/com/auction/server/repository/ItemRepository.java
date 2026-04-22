package com.auction.server.repository;

import com.auction.common.entity.Item;
import java.util.List;

public interface ItemRepository {
    void save(Item item);
    Item findById(String id);
    List<Item> findAll();
    void update(Item item);
    void delete(String id);
}
