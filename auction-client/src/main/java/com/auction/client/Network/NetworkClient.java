package com.auction.client.Network;

import java.io.*;
import java.net.Socket;

public class NetworkClient {
    private static NetworkClient instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private NetworkClient() {}

    public static NetworkClient getInstance() {
        if (instance == null) instance = new NetworkClient();
        return instance;
    }

    public void connect(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void sendRequest(Object request) throws IOException {
        out.writeObject(request);
        out.flush();
    }

    public Object receiveResponse() throws IOException, ClassNotFoundException {
        return in.readObject();
    }
}