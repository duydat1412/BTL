package com.auction.server.handler;

import com.auction.common.message.Action;
import com.auction.common.message.ClientRequest;
import com.auction.common.message.ClientResponse;
import com.auction.common.entity.AutoBid;
import com.auction.common.entity.BidTransaction;
import com.auction.common.message.*;
import com.auction.common.strategy.BidStrategy;
import com.auction.common.strategy.ManualBidStrategy;
import com.auction.common.strategy.AutoBidStrategy;

import com.auction.server.exception.AuthenticationException;
import com.auction.server.exception.AuctionClosedException;
import com.auction.server.exception.InvalidBidException;
import com.auction.server.repository.SerializableAuctionRepository;
import com.auction.server.repository.SerializableBidRepository;
import com.auction.server.observer.AuctionEventManager;
import com.auction.server.observer.BroadcastObserver;
import com.auction.server.service.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.List;

/**
 * Xử lý yêu cầu từ Client (chạy trên một luồng riêng).
 * Đọc Object từ Input Stream, chuyển đến các Handler cụ thể,
 * và trả về Object qua Output Stream.
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    // Khởi tạo các services và event manager dùng chung cho các handlers
    private static final ItemService itemService = new ItemService();
    private static final AuctionEventManager eventManager = new AuctionEventManager();
    private static final SerializableAuctionRepository auctionRepo = new SerializableAuctionRepository();
    private static final SerializableBidRepository bidRepo = new SerializableBidRepository();
    private static final BidService bidService = new BidService(auctionRepo, bidRepo, eventManager);
    private static final AutoBidService autoBidService = new AutoBidService(bidService, auctionRepo, bidRepo);
    static {
        eventManager.subscribe(new BroadcastObserver());
        eventManager.subscribe(autoBidService);
        AuctionScheduler.setEventManager(eventManager);
    }

    public static AuctionEventManager getEventManager() {
        return eventManager;
    }

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        String clientId = clientSocket.getInetAddress() + ":" + clientSocket.getPort();
        try (
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            // Đăng ký client vào registry để Server có thể push tin nhắn
            ClientRegistry.getInstance().register(clientId, out);
            System.out.println("Đã sẵn sàng giao tiếp với client: " + clientId);

            while (true) {
                Object requestObj = in.readObject();
                ClientResponse response = handleIncomingRequest(requestObj);
                // synchronized vì broadcast() cũng có thể ghi vào out cùng lúc
                synchronized (out) {
                    out.reset();
                    out.writeObject(response);
                    out.flush();
                }
            }

        } catch (EOFException e) {
            System.out.println("Client đã ngắt kết nối: " + clientId);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi quá trình giao tiếp Socket với client: " + e.getMessage());
        } finally {
            // Hủy đăng ký khi client ngắt
            ClientRegistry.getInstance().unregister(clientId);
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Lỗi khi đóng socket: " + e.getMessage());
            }
        }
    }

    private ClientResponse handleIncomingRequest(Object requestObj) {
        if (!(requestObj instanceof ClientRequest request)) {
            return failure("Invalid request format");
        }

        Action action = request.getAction();
        if (action == null) {
            return failure("Action is required");
        }

        Serializable payload = request.getPayload();
        return switch (action) {
            case REGISTER -> handleRegister(payload);
            case LOGIN -> handleLogin(payload);
            case GET_USERS -> handleGetUsers(payload);
            case BAN_USER -> handleBanUser(payload);
            case UNBAN_USER -> handleUnbanUser(payload);
            case CANCEL_AUCTION -> handleCancelAuction(payload);
            case GET_AUCTIONS -> handleGetAuctions(payload);
            case GET_AUCTION -> handleGetAuction(payload);
            case CREATE_AUCTION -> handleCreateAuction(payload);
            case PLACE_BID -> handlePlaceBid(payload);
            case GET_BID_HISTORY -> handleGetBidHistory(payload);
            case GET_ITEMS -> handleGetItems(payload);
            case CREATE_ITEM -> handleCreateItem(payload);
            case UPDATE_ITEM -> handleUpdateItem(payload);
            case DELETE_ITEM -> handleDeleteItem(payload);
            case REGISTER_AUTO_BID -> handleRegisterAutoBid(payload);
            case REMOVE_AUTO_BID -> handleRemoveAutoBid(payload);
        };
    }

    private ClientResponse handleGetUsers(Serializable payload) {
        if (!(payload instanceof GetAllUsersRequest req)) {
            return failure("GET_USERS payload must be GetAllUsersRequest");
        }
        return executeAuthAction(() -> UserService.getAllUsers(req));
    }

    private ClientResponse handleBanUser(Serializable payload) {
        if (!(payload instanceof BanUserRequest req)) {
            return failure("BAN_USER payload must be BanUserRequest");
        }
        return executeAuthAction(() -> UserService.banUser(req));
    }

    private ClientResponse handleUnbanUser(Serializable payload) {
        if (!(payload instanceof UnbanUserRequest req)) {
            return failure("UNBAN_USER payload must be UnbanUserRequest");
        }
        return executeAuthAction(() -> UserService.unbanUser(req));
    }

    private ClientResponse handleCancelAuction(Serializable payload) {
        if (!(payload instanceof CancelAuctionRequest req)) {
            return failure("CANCEL_AUCTION payload must be CancelAuctionRequest");
        }
        return AuctionService.cancelAuction(req);
    }

    private ClientResponse handleRegister(Serializable payload) {
        if (!(payload instanceof RegisterRequest req)) {
            return failure("REGISTER payload must be RegisterRequest");
        }
        return executeAuthAction(() -> UserService.signup(req, ""));
    }

    private ClientResponse handleLogin(Serializable payload) {
        if (!(payload instanceof LoginRequest req)) {
            return failure("LOGIN payload must be LoginRequest");
        }
        return executeAuthAction(() -> UserService.login(req));
    }

    private ClientResponse executeAuthAction(AuthAction action) {
        try {
            return action.run();
        } catch (AuthenticationException e) {
            return failure(e.getMessage());
        }
    }

    private ClientResponse handleGetItems(Serializable payload) {
        if (!(payload instanceof GetItemsRequest req)) {
            return failure("GET_ITEMS payload must be GetItemsRequest");
        }
        return ItemService.R(req);
    }

    private ClientResponse handleCreateItem(Serializable payload) {
        if (!(payload instanceof CreateItemRequest req)) {
            return failure("CREATE_ITEM payload must be CreateItemRequest");
        }
        return ItemService.C(req);
    }

    private ClientResponse handleUpdateItem(Serializable payload) {
        if (!(payload instanceof UpdateItemRequest req)) {
            return failure("UPDATE_ITEM payload must be UpdateItemRequest");
        }
        return itemService.U(req);
    }

    private ClientResponse handleDeleteItem(Serializable payload) {
        if (!(payload instanceof DeleteItemRequest req)) {
            return failure("DELETE_ITEM payload must be DeleteItemRequest");
        }
        // Truyền getSellerId cho cả 2 tham số vì ClientRequest không có
        // session/senderId hiện tại
        return itemService.D(req, req.getSellerId());
    }

    private ClientResponse handlePlaceBid(Serializable payload) {
        if (!(payload instanceof PlaceBidRequest req)) {
            return failure("PLACE_BID payload must be PlaceBidRequest");
        }
        if (req.isAutoBid()) {
            if (autoBidService.hasAutoBid(req.getAuctionId(), req.getBidderId())) {
                return failure("Bạn đã đăng ký auto-bid cho phiên này rồi.");
            }
            AutoBid config = new AutoBid(req.getAuctionId(), req.getBidderId(), req.getAmount(), 500);
            autoBidService.registerAutoBid(config);
            return new ClientResponse(true, "Đăng ký auto-bid thành công (max: " + String.format("%,.0f", req.getAmount()) + " VNĐ)", null);
        }
        BidStrategy strategy = new ManualBidStrategy();
        try {
            BidTransaction result = bidService.placeBid(req.getAuctionId(), req.getBidderId(), req.getAmount(),
                    strategy);
            return new ClientResponse(true, "Đặt giá thành công", result);
        } catch (AuctionClosedException | InvalidBidException e) {
            return failure(e.getMessage());
        }
    }

    private ClientResponse handleGetBidHistory(Serializable payload) {
        if (!(payload instanceof GetBidHistoryRequest req)) {
            return failure("GET_BID_HISTORY payload must be GetBidHistoryRequest");
        }
        List<BidTransaction> history = bidService.getBidHistory(req.getAuctionId());
        return new ClientResponse(true, "Lấy lịch sử thành công", (Serializable) history);
    }

    private ClientResponse handleGetAuctions(Serializable payload) {
        GetAuctionsRequest req = (payload instanceof GetAuctionsRequest)
                ? (GetAuctionsRequest) payload : null;
        return AuctionService.getAuctions(req);
    }

    private ClientResponse handleGetAuction(Serializable payload) {
        if (!(payload instanceof String auctionId)) {
            return failure("GET_AUCTION payload must be auctionId (String)");
        }
        return AuctionService.getAuction(auctionId);
    }

    private ClientResponse handleCreateAuction(Serializable payload) {
        if (!(payload instanceof CreateAuctionRequest req)) {
            return failure("CREATE_AUCTION payload must be CreateAuctionRequest");
        }
        return AuctionService.createAuction(req);
    }

    private ClientResponse handleRegisterAutoBid(Serializable payload) {
        if (!(payload instanceof RegisterAutoBidRequest req)) {
            return failure("REGISTER_AUTO_BID payload must be RegisterAutoBidRequest");
        }
        AutoBid config = new AutoBid(req.getAuctionId(), req.getBidderId(), req.getMaxBid(), req.getIncrement());
        autoBidService.registerAutoBid(config);
        return new ClientResponse(true, "Đăng ký auto-bid thành công", null);
    }

    private ClientResponse handleRemoveAutoBid(Serializable payload) {
        if (!(payload instanceof RemoveAutoBidRequest req)) {
            return failure("REMOVE_AUTO_BID payload must be RemoveAutoBidRequest");
        }
        autoBidService.removeAutoBid(req.getAuctionId(), req.getBidderId());
        return new ClientResponse(true, "Đã hủy auto-bid", null);
    }

    private ClientResponse failure(String message) {
        return new ClientResponse(false, message, null);
    }

    @FunctionalInterface
    private interface AuthAction {
        ClientResponse run() throws AuthenticationException;
    }

}
