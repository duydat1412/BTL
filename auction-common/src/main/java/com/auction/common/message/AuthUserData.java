package com.auction.common.message;

import com.auction.common.enums.UserRole;
import java.io.Serializable;

/**
 * Lightweight user data for authentication responses.
 */
public class AuthUserData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String username;
    private final UserRole role;

    public AuthUserData(String userId, String username, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }
}

