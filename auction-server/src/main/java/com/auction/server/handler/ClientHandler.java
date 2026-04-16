package com.auction.server.handler;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
                // Nhận Request Object (Tuần 7-8 sẽ định nghĩa class Request rõ ràng hơn)
                Object requestObj = in.readObject();
                System.out.println("Nhận request: " + requestObj);

                // TODO: Xử lý thông qua UserService, ItemService (do Người D và Người B phụ trách)
                // String action = request.getAction();
                // ...
                
                // Trả về kết quả
                String mockResponse = "Kết quả từ Server cho request: " + requestObj.toString();
                out.writeObject(mockResponse);
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
}
