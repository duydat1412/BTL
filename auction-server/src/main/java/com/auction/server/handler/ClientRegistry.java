package com.auction.server.handler;

import com.auction.common.message.ServerPushMessage;

import java.io.ObjectOutputStream;
import java.io.Serializable;
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

    /** Map: clientId → ObjectOutputStream */
    private final Map<String, ObjectOutputStream> clients = new ConcurrentHashMap<>();

    /** Map: userId → clientId (để tìm socket của user theo userId) */
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    private ClientRegistry() {}

    public static ClientRegistry getInstance() {
        return INSTANCE;
    }

    public void register(String clientId, ObjectOutputStream out) {
        clients.put(clientId, out);
        System.out.println("[Registry] Client đã kết nối: " + clientId
                + " (Tổng: " + clients.size() + ")");
    }

    public void unregister(String clientId) {
        clients.remove(clientId);
        // Xóa userId mapping nếu có
        userSessions.entrySet().removeIf(e -> e.getValue().equals(clientId));
        System.out.println("[Registry] Client đã ngắt: " + clientId
                + " (Tổng: " + clients.size() + ")");
    }

    /**
     * Đăng ký userId sau khi login thành công.
     */
    public void registerUserSession(String userId, String clientId) {
        userSessions.put(userId, clientId);
    }

    /**
     * Kick một user ra khỏi hệ thống ngay lập tức.
     * Gửi USER_BANNED push và đóng socket.
     */
    public void kickUser(String userId, String reason) {
        String clientId = userSessions.get(userId);
        if (clientId == null) {
            System.out.println("[Registry] User " + userId + " không online, không cần kick.");
            return;
        }
        ObjectOutputStream out = clients.get(clientId);
        if (out == null) return;

        try {
            ServerPushMessage kickMsg = new ServerPushMessage(
                    ServerPushMessage.PushType.USER_BANNED, reason, null);
            synchronized (out) {
                out.reset();
                out.writeObject(kickMsg);
                out.flush();
            }
            System.out.println("[Registry] Đã gửi USER_BANNED tới " + clientId);
        } catch (Exception e) {
            System.err.println("[Registry] Lỗi khi kick user " + userId + ": " + e.getMessage());
        }
    }

    public void broadcast(ServerPushMessage pushMessage) {
        for (Map.Entry<String, ObjectOutputStream> entry : clients.entrySet()) {
            try {
                ObjectOutputStream out = entry.getValue();
                synchronized (out) {
                    out.reset();
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
