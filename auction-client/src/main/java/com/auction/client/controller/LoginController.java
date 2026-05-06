package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.message.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void handleLogin() {

        String user = usernameField.getText();
        String pass = passwordField.getText();

        // check input
        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Không được để trống!");
            return;
        }

        try {
            NetworkClient client = NetworkClient.getInstance();
            client.connect();
            ClientResponse res = client.login(user, pass);
            errorLabel.setText(res.getMessage());
            System.out.println("Server: " + res.getMessage());

        } catch (Exception e) {
            errorLabel.setText("Không kết nối được server!");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegister() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Nhập đủ thông tin!");
            return;
        }

        try {
            NetworkClient client = NetworkClient.getInstance();
            client.connect();

            RegisterRequest req = new RegisterRequest(user, pass, user, null);
            ClientRequest request = new ClientRequest(Action.REGISTER, req);

            ClientResponse res = client.sendRequest(request);

            errorLabel.setText(res.getMessage());
            System.out.println("Server: " + res.getMessage());


        } catch (Exception e) {
            errorLabel.setText("Không thể kết nối server!");
            e.printStackTrace();
        }
    }
}