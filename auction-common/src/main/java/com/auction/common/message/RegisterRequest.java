package com.auction.common.message;

import com.auction.common.enums.UserRole;
import java.io.Serializable;

/**
 * Payload for REGISTER action.
 */
public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;
    private final String email;
    private final UserRole role;

    public RegisterRequest(String username, String password, String email, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }
}

