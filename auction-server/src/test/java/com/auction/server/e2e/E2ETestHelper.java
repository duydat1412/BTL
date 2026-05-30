package com.auction.server.e2e;

import com.auction.common.message.ClientRequest;
import com.auction.common.message.ClientResponse;
import com.auction.common.message.ServerPushMessage;
import com.auction.server.datastore.DataStore;
import com.auction.server.handler.ClientHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class E2ETestHelper {
    public static final int SOCKET_TIMEOUT_MILLIS = 5_000;

    private E2ETestHelper() {}

    public static TestServer startServer() throws IOException {
        return new TestServer();
    }

    public static Socket connectToServer(int port) throws IOException {
        Socket socket = new Socket("localhost", port);
        socket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
        return socket;
    }

    public static ClientResponse sendReceive(ObjectOutputStream out, ObjectInputStream in, ClientRequest request)
            throws Exception {
        out.writeObject(request);
        out.flush();
        while (true) {
            Object obj = in.readObject();
            if (obj instanceof ClientResponse) {
                return (ClientResponse) obj;
            }
        }
    }

    public static ObjectOutputStream createOutStream(Socket socket) throws IOException {
        return new ObjectOutputStream(socket.getOutputStream());
    }

    public static ObjectInputStream createInStream(Socket socket) throws IOException {
        return new ObjectInputStream(socket.getInputStream());
    }

    public static ServerPushMessage readPush(ObjectInputStream in) throws Exception {
        while (true) {
            Object obj = in.readObject();
            if (obj instanceof ServerPushMessage) {
                return (ServerPushMessage) obj;
            }
        }
    }

    public static ServerPushMessage waitForPush(ObjectInputStream in,
                                                ServerPushMessage.PushType expectedType,
                                                long timeoutMillis) throws Exception {
        Instant deadline = Instant.now().plusMillis(timeoutMillis);
        List<ServerPushMessage.PushType> observedTypes = new ArrayList<>();

        while (Instant.now().isBefore(deadline)) {
            try {
                ServerPushMessage push = readPush(in);
                observedTypes.add(push.getType());
                if (push.getType() == expectedType) {
                    return push;
                }
            } catch (SocketTimeoutException ex) {
                break;
            }
        }

        throw new AssertionError("Timed out waiting for push " + expectedType
                + " within " + Duration.ofMillis(timeoutMillis)
                + ". Observed pushes: " + observedTypes);
    }

    public static ClientResponse sendRequest(Socket socket, ClientRequest request) throws Exception {
        ObjectOutputStream out = createOutStream(socket);
        ObjectInputStream in = createInStream(socket);
        return sendReceive(out, in, request);
    }

    public static void resetDataStore() {
        DataStore.getInstance().getUsers().clear();
        DataStore.getInstance().getItems().clear();
        DataStore.getInstance().getAuctions().clear();
        DataStore.getInstance().getBidTransactions().clear();
    }

    public static class TestServer implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final ExecutorService pool;
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final Thread acceptThread;

        public TestServer() throws IOException {
            this.pool = Executors.newFixedThreadPool(10);
            this.serverSocket = new ServerSocket(0);

            this.acceptThread = new Thread(() -> {
                while (running.get()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        pool.execute(new ClientHandler(clientSocket));
                    } catch (IOException e) {
                        if (running.get()) {
                            System.err.println("[E2E-Server] Accept error: " + e.getMessage());
                        }
                    }
                }
            }, "E2E-Accept");
            acceptThread.setDaemon(true);
            acceptThread.start();

            System.out.println("[E2E-Server] Started on port " + getPort());
        }

        public int getPort() {
            return serverSocket.getLocalPort();
        }

        @Override
        public void close() {
            running.set(false);
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
            pool.shutdownNow();
            acceptThread.interrupt();
        }
    }
}
