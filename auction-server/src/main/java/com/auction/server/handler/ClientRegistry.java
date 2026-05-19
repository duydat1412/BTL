package com.auction.server.handler;

import com.auction.common.message.ServerPushMessage;

import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton quản lý tất cả Client đang kết nối.
 * Cho phép Server push tin nhắn xuống bất kỳ hoặc tất cả Client.
 *
 * <p>Thread-safe nhờ ConcurrentHashMap.
 */
public class ClientRegistry {

    private static final ClientRegistry INSTANCE = new ClientRegistry();

    /**
     * Map: clientId → ObjectOutputStream của client đó.
     * Dùng clientId (socket address) làm key.
     */
    private final Map<String, ObjectOutputStream> clients = new ConcurrentHashMap<>();

    private ClientRegistry() {}

    public static ClientRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Đăng ký một client mới khi kết nối.
     */
    public void register(String clientId, ObjectOutputStream out) {
        clients.put(clientId, out);
        System.out.println("[Registry] Client đã kết nối: " + clientId
                + " (Tổng: " + clients.size() + ")");
    }

    /**
     * Hủy đăng ký khi client ngắt kết nối.
     */
    public void unregister(String clientId) {
        clients.remove(clientId);
        System.out.println("[Registry] Client đã ngắt: " + clientId
                + " (Tổng: " + clients.size() + ")");
    }

    /**
     * Push tin nhắn xuống TẤT CẢ client đang kết nối.
     * Nếu gửi cho 1 client bị lỗi (đã ngắt), tự động xóa khỏi registry.
     */
    public void broadcast(ServerPushMessage pushMessage) {
        for (Map.Entry<String, ObjectOutputStream> entry : clients.entrySet()) {
            try {
                ObjectOutputStream out = entry.getValue();
                synchronized (out) {
                    out.writeObject(pushMessage);
                    out.flush();
                }
            } catch (Exception e) {
                System.err.println("[Registry] Lỗi push tới " + entry.getKey()
                        + ", xóa khỏi registry.");
                clients.remove(entry.getKey());
            }
        }
    }

    public int getClientCount() {
        return clients.size();
    }
}
