package com.auction.common.message;

import java.io.Serializable;

/**
 * Generic request wrapper sent from client to server.
 */
public class ClientRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Action action;
    private final Serializable payload;

    public ClientRequest(Action action, Serializable payload) {
        this.action = action;
        this.payload = payload;
    }

    public Action getAction() {
        return action;
    }

    public Serializable getPayload() {
        return payload;
    }
}
