package com.auction.common.message;

/**
 * Supported client actions over socket.
 */
public enum Action {
    REGISTER,
    LOGIN,
    GET_AUCTIONS,
    GET_AUCTION,
    CREATE_AUCTION,
    PLACE_BID,
    GET_BID_HISTORY,
    GET_ITEMS,
    CREATE_ITEM,
    UPDATE_ITEM,
    DELETE_ITEM
}

