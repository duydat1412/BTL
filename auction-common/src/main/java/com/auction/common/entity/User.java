package com.auction.common.entity;

import java.io.Serializable;

/**
 * Lớp đại diện cho User.
 * (Sẽ được Người B hoàn thiện chi tiết sau, hiện tại để trống/stub để test Server A).
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String username;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
