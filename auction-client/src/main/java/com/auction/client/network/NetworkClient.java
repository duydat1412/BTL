package com.auction.client.network;

import com.auction.common.message.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Client Network Singleton — xử lý giao tiếp Socket với Server.
 *
 * <p>Hỗ trợ 2 luồng giao tiếp:
 * <ol>
 *   <li>Request-Response: Client gửi {@link ClientRequest}, chờ {@link ClientResponse}.</li>
 *   <li>Server Push: Server tự gửi {@link ServerPushMessage}, listener thread nhận
 *       và dispatch tới các callback đã đăng ký.</li>
 * </ol>
 *
 * <p>Khi {@link #connect()} được gọi, một background thread (listener) bắt đầu
 * đọc liên tục từ ObjectInputStream. Mọi object nhận được sẽ được phân loại:
 * <ul>
 *   <li>{@link ClientResponse} → đưa vào {@code responseQueue} để {@link #sendRequest} lấy.</li>
 *   <li>{@link ServerPushMessage} → dispatch tới tất cả {@link PushListener} đã đăng ký.</li>
 * </ul>
 */
public class NetworkClient {
    private static NetworkClient instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private AuthUserData currentUser;

    private final String HOST = "localhost";
    private final int PORT = 8080;

    /**
     * Queue để listener thread gửi ClientResponse về cho sendRequest thread.
     */
    private final BlockingQueue<ClientResponse> responseQueue = new LinkedBlockingQueue<>();

    /**
     * Danh sách các callback nhận push notification.
     */
    private final List<PushListener> pushListeners = new CopyOnWriteArrayList<>();

    /**
     * Lock đảm bảo chỉ một request-response cycle chạy tại một thời điểm.
     * Tránh lỗi response bị lẫn khi push trigger request mới trong lúc request cũ đang chờ.
     */
    private final ReentrantLock requestLock = new ReentrantLock();

    private static final ExecutorService IO_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "IO-Worker");
        t.setDaemon(true);
        return t;
    });

    private NetworkClient() {}

    public static NetworkClient getInstance() {
        if (instance == null) instance = new NetworkClient();
        return instance;
    }

    /**
     * Kết nối đến Server và khởi động listener thread.
     */
    public void connect() {
        try {
            if (socket != null && socket.isConnected() && !socket.isClosed()) {
                return;
            }
            socket = new Socket(HOST, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Khởi động listener thread đọc mọi object từ server
            Thread listenerThread = new Thread(this::listenForServerMessages, "ServerListener");
            listenerThread.setDaemon(true);
            listenerThread.start();

            System.out.println("C: Kết nối Server thành công");

        } catch (IOException e) {
            System.err.println("C: Lỗi kết nối Server: " + e.getMessage());
        }
    }

    /**
     * Background thread — đọc liên tục từ InputStream.
     * Phân loại: ClientResponse → queue, ServerPushMessage → callback.
     */
    private void listenForServerMessages() {
        try {
            System.out.println("[DEBUG] Listener thread started");
            while (socket != null && !socket.isClosed()) {
                Object obj = in.readObject();
                System.out.println("[DEBUG] Listener received: " + obj.getClass().getSimpleName());

                if (obj instanceof ClientResponse response) {
                    System.out.println("[DEBUG] Listener queueing response, success=" + response.isSuccess());
                    responseQueue.put(response);
                
                } else if (obj instanceof ServerPushMessage pushMsg) {
                    // Push notification từ server → dispatch tới UI
                    for (PushListener listener : pushListeners) {
                        try {
                            listener.onPush(pushMsg);
                        } catch (Exception e) {
                            System.err.println("C: Lỗi trong PushListener: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("C: Listener thread kết thúc: " + e.getMessage());
        }
    }

    /**
     * Gửi request và chờ response (blocking).
     * Listener thread sẽ đặt response vào queue khi nhận được.
     */
    public ClientResponse sendRequest(ClientRequest request) {
        if (socket == null || socket.isClosed()) {
            System.out.println("[DEBUG] sendRequest: socket not connected");
            return new ClientResponse(false, "Chưa kết nối server", null);
        }

        // Lock để serialize request-response: tránh 2 thread cùng chờ responseQueue
        // gây lẫn response (VD: push trigger loadAuctions trong lúc CREATE_ITEM đang chờ)
        requestLock.lock();
        try {
            System.out.println("[DEBUG] sendRequest: writing " + request.getAction());
            synchronized (out) {
                out.reset();
                out.writeObject(request);
                out.flush();
            }
            System.out.println("[DEBUG] sendRequest: waiting for response...");
            ClientResponse res = responseQueue.take();
            System.out.println("[DEBUG] sendRequest: got response, success=" + res.isSuccess());
            return res;

        } catch (Exception e) {
            return new ClientResponse(false, "Lỗi giao tiếp: " + e.getMessage(), null);
        } finally {
            requestLock.unlock();
        }
    }

    /**
     * Gửi request bất đồng bộ — không block JavaFX thread.
     * I/O chạy trên background thread, response trả về qua CompletableFuture.
     */
    public CompletableFuture<ClientResponse> sendRequestAsync(ClientRequest request) {
        return CompletableFuture.supplyAsync(() -> sendRequest(request), IO_EXECUTOR);
    }

    /**
     * Hàm tiện ích dành riêng cho Login (blocking).
     */
    public ClientResponse login(String username, String password) {
        LoginRequest payload = new LoginRequest(username, password);
        ClientRequest request = new ClientRequest(Action.LOGIN, payload);
        return sendRequest(request);
    }

    /**
     * Hàm tiện ích dành riêng cho Login (bất đồng bộ).
     */
    public CompletableFuture<ClientResponse> loginAsync(String username, String password) {
        LoginRequest payload = new LoginRequest(username, password);
        ClientRequest request = new ClientRequest(Action.LOGIN, payload);
        return sendRequestAsync(request);
    }

    // ==================== Push Listener ====================

    /**
     * Interface callback nhận push notification từ server.
     */
    @FunctionalInterface
    public interface PushListener {
        void onPush(ServerPushMessage message);
    }

    /**
     * Đăng ký nhận push notification.
     */
    public void addPushListener(PushListener listener) {
        pushListeners.add(listener);
    }

    /**
     * Hủy đăng ký.
     */
    public void removePushListener(PushListener listener) {
        pushListeners.remove(listener);
    }

    // ==================== Session ====================

    public void setCurrentUser(AuthUserData user) {
        this.currentUser = user;
    }

    public AuthUserData getCurrentUser() {
        return currentUser;
    }
}