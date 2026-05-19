package com.auction.common.message;

import java.io.Serializable;

public class GetAllUsersRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String adminId;

    public GetAllUsersRequest(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminId() {
        return adminId;
    }
}
