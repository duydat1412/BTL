package com.auction.server.repository;

import com.auction.common.entity.User;
import com.auction.server.datastore.DataStore;

import java.util.List;

/**
 * Triển khai UserRepository thông qua DataStore (Serialization).
 */
public class SerializableUserRepository implements UserRepository {

    @Override
    public void save(User user) {
        DataStore.getInstance().getUsers().add(user);
        DataStore.getInstance().saveData();
    }

    @Override
    public User findById(String id) {
        for (User u : DataStore.getInstance().getUsers()) {
            if (u.getId() != null && u.getId().equals(id)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        for (User u : DataStore.getInstance().getUsers()) {
            if (u.getUsername() != null && u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        return DataStore.getInstance().getUsers();
    }

    @Override
    public void update(User user) {
        // Trong trường hợp này Object reference đã thay đổi trực tiếp
        // Nên chỉ cần lưu lại xuống file
        DataStore.getInstance().saveData();
    }

    @Override
    public void delete(String id) {
        DataStore.getInstance().getUsers().removeIf(u -> u.getId() != null && u.getId().equals(id));
        DataStore.getInstance().saveData();
    }
}
