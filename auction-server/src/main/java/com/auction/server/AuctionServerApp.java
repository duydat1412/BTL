package com.auction.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Điểm bắt đầu (entry point) của server (Sử dụng Java Socket).
 *
 * <p>Server sẽ lắng nghe trên cổng 8080.
 * Khi có một client kết nối, server sẽ tạo (hoặc lấy từ pool) một Thread mới
 * để xử lý riêng biệt cho client đó. Điều này đảm bảo nhiều client có thể
 * kết nối và tương tác cùng lúc.
 */
public class AuctionServerApp {

    private static final int PORT = 8080;
    // Thread pool giới hạn số lượng luồng (thay vì new Thread() liên tục)
    private static final ExecutorService pool = Executors.newFixedThreadPool(20);

    public static void main(String[] args) {
        System.out.println("=== Khởi động Auction Server ===");
        
        // Nạp dữ liệu từ DataStore (nếu có)
        com.auction.server.datastore.DataStore.getInstance().loadData();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đang lắng nghe trên cổng " + PORT);

            while (true) {
                // Chờ đợi có client kết nối tới
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client mới kết nối: " + clientSocket.getInetAddress());

                // Giao việc xử lý client này cho một luồng riêng
                // Vòng lặp chính tiếp tục quay lại chờ client khác
                pool.execute(new com.auction.server.handler.ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Lỗi quá trình chạy server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ghi dữ liệu xuống file trước khi tắt server
            com.auction.server.datastore.DataStore.getInstance().saveData();
            pool.shutdown();
        }
    }
}
