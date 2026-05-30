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
    private double balance;

    public AuthUserData(String userId, String username, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.balance = 0;
    }

    public AuthUserData(String userId, String username, UserRole role, double balance) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.balance = balance;
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

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

