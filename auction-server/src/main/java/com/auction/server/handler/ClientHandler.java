package com.auction.server.handler;

import com.auction.common.message.Action;
import com.auction.common.message.ClientRequest;
import com.auction.common.message.ClientResponse;
import com.auction.common.message.CreateItemRequest;
import com.auction.common.message.BanUserRequest;
import com.auction.common.message.CancelAuctionRequest;
import com.auction.common.message.GetItemsRequest;
import com.auction.common.message.UpdateItemRequest;
import com.auction.common.message.DeleteItemRequest;
import com.auction.common.message.LoginRequest;
import com.auction.common.message.RegisterRequest;
import com.auction.common.message.PlaceBidRequest;
import com.auction.common.message.GetBidHistoryRequest;
import com.auction.common.entity.BidTransaction;
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
import com.auction.server.service.ItemService;
import com.auction.server.service.UserService;
import com.auction.server.service.BidService;
import com.auction.server.service.AuctionService;
import com.auction.common.message.CreateAuctionRequest;
import com.auction.common.message.GetAuctionsRequest;

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
    static {
        // Đăng ký BroadcastObserver để push real-time khi có bid mới
        eventManager.subscribe(new BroadcastObserver());
    }
    private static final BidService bidService = new BidService(
            new SerializableAuctionRepository(),
            new SerializableBidRepository(),
            eventManager);

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
        };
    }

    private ClientResponse handleGetUsers(Serializable payload) {
        return failure("GET_USERS pending: admin service integration");
    }

    private ClientResponse handleBanUser(Serializable payload) {
        if (!(payload instanceof BanUserRequest)) {
            return failure("BAN_USER payload must be BanUserRequest");
        }
        return failure("BAN_USER pending: admin service integration");
    }

    private ClientResponse handleCancelAuction(Serializable payload) {
        if (!(payload instanceof CancelAuctionRequest)) {
            return failure("CANCEL_AUCTION payload must be CancelAuctionRequest");
        }
        return failure("CANCEL_AUCTION pending: admin service integration");
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
        return executeAuthAction(() -> UserService.xlogin(req));
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
            return failure("Tính năng đấu giá tự động (Auto-bid) hiện chưa được hỗ trợ.");
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

    private ClientResponse failure(String message) {
        return new ClientResponse(false, message, null);
    }

    @FunctionalInterface
    private interface AuthAction {
        ClientResponse run() throws AuthenticationException;
    }

}
