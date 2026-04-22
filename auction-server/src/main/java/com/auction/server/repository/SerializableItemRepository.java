package com.auction.server.repository;

import com.auction.common.entity.Item;
import com.auction.server.datastore.DataStore;
import java.util.List;

public class SerializableItemRepository implements ItemRepository {
    @Override
    public void save(Item item) {
        DataStore.getInstance().getItems().add(item);
        DataStore.getInstance().saveData();
    }

    @Override
    public Item findById(String id) {
        return DataStore.getInstance().getItems().stream()
                .filter(i -> i.getId() != null && i.getId().equals(id))
                .findFirst().orElse(null);
    }

    @Override
    public List<Item> findAll() { return DataStore.getInstance().getItems(); }

    @Override
    public void update(Item item) { DataStore.getInstance().saveData(); }

    @Override
    public void delete(String id) {
        DataStore.getInstance().getItems().removeIf(i -> i.getId() != null && i.getId().equals(id));
        DataStore.getInstance().saveData();
    }
}
