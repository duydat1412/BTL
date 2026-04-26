package com.auction.server.handler;

import com.auction.common.message.Action;
import com.auction.common.message.ClientRequest;
import com.auction.common.message.ClientResponse;
import com.auction.common.message.LoginRequest;
import com.auction.common.message.RegisterRequest;
import com.auction.server.exception.AuthenticationException;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import com.auction.server.service.UserService;

/**
 * Xử lý yêu cầu từ Client (chạy trên một luồng riêng).
 * Đọc Object từ Input Stream, chuyển đến các Handler cụ thể,
 * và trả về Object qua Output Stream.
 */
public class ClientHandler implements Runnable {
    
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
            // Quan trọng: output stream phải được tạo trước để ghi header gửi qua client
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            System.out.println("Đã sẵn sàng giao tiếp với client: " + clientSocket.getInetAddress());

            while (true) {
                Object requestObj = in.readObject();
                ClientResponse response = handleIncomingRequest(requestObj);
                out.writeObject(response);
                out.flush();
            }

        } catch (EOFException e) {
            System.out.println("Client đã ngắt kết nối: " + clientSocket.getInetAddress());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi quá trình giao tiếp Socket với client: " + e.getMessage());
            // e.printStackTrace();
        } finally {
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
            case GET_AUCTIONS -> pending(action, "AuctionService integration");
            case GET_AUCTION -> pending(action, "AuctionService integration");
            case CREATE_AUCTION -> pending(action, "AuctionService integration");
            case PLACE_BID -> pending(action, "BidService integration"); // nhanh lên Minh Đức, tôi đang đợi ông đây
            case GET_BID_HISTORY -> pending(action, "BidService integration");
            case GET_ITEMS -> pending(action, "ItemService integration");
            case CREATE_ITEM -> pending(action, "ItemService integration");
            case UPDATE_ITEM -> pending(action, "ItemService integration");
            case DELETE_ITEM -> pending(action, "ItemService integration");
        };
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

    private ClientResponse failure(String message) {
        return new ClientResponse(false, message, null);
    }

    private ClientResponse pending(Action action, String waitingFor) {
        return failure(action + " pending: " + waitingFor);
    }

    @FunctionalInterface
    private interface AuthAction {
        ClientResponse run() throws AuthenticationException;
    }

}
