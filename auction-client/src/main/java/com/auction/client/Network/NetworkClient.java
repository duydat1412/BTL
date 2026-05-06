package com.auction.client.network;

import com.auction.common.message.*;
import java.io.*;
import java.net.Socket;

public class NetworkClient {
    private static NetworkClient instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Cấu hình theo AuctionServerApp
    private final String HOST = "localhost";
    private final int PORT = 8080;

    private NetworkClient() {}

    public static NetworkClient getInstance() {
        if (instance == null) instance = new NetworkClient();
        return instance;
    }
    /**
     * Kết nối đến Server Socket
     */
    public void connect() {
        try {
            if (socket != null && socket.isConnected()) {
                return; // đã kết nối rồi
            }
            socket = new Socket(HOST, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("C: Kết nối Server thành công");

        } catch (IOException e) {
            System.err.println("C: Lỗi kết nối Server: " + e.getMessage());
        }
    }

    /**
     * Hàm gửi nhận tổng quát dùng ClientRequest và ClientResponse
     */
    public ClientResponse sendRequest(ClientRequest request) {

        if (socket == null || socket.isClosed()) {
            return new ClientResponse(false, "Chưa kết nối server", null);
        }

        try {
            out.writeObject(request);
            out.flush();
            return (ClientResponse) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            return new ClientResponse(false, "Lỗi giao tiếp: " + e.getMessage(), null);
        }
    }
    /**
     * Hàm tiện ích dành riêng cho Login
     */
    public ClientResponse login(String username, String password) {
        LoginRequest payload = new LoginRequest(username, password);
        ClientRequest request = new ClientRequest(Action.LOGIN, payload);
        return sendRequest(request);
    }

}