package com.auction.server.repository;

import com.auction.common.entity.User;
import java.util.List;

/**
 * Giao diện Repository cho User.
 */
public interface UserRepository {
    void save(User user);
    User findById(String id);
    User findByUsername(String username);
    List<User> findAll();
    void update(User user);
    void delete(String id);
}
