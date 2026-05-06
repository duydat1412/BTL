package com.auction.client.controller;

import com.auction.common.enums.UserRole;
import javafx.collections.FXCollections;
import com.auction.client.network.NetworkClient;
import com.auction.common.message.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private ComboBox<UserRole> roleBox;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        roleBox.setItems(FXCollections.observableArrayList(UserRole.values()));
    }
    @FXML
    public void handleLogin() {

        String user = usernameField.getText();
        String pass = passwordField.getText();
        String email = emailField.getText();
        UserRole role = roleBox.getValue();

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
        String email = emailField.getText();
        UserRole role = roleBox.getValue();

        if (user.isEmpty() || pass.isEmpty() || email.isEmpty() || role == null) {
            errorLabel.setText("Nhập đủ thông tin!");
            return;
        }

        try {
            NetworkClient client = NetworkClient.getInstance();
            client.connect();

            RegisterRequest req = new RegisterRequest(user, pass, email, role);
            ClientRequest request = new ClientRequest(Action.REGISTER, req);

            ClientResponse res = client.sendRequest(request);

            errorLabel.setText(res.getMessage());

        } catch (Exception e) {
            errorLabel.setText("Lỗi kết nối!");
            e.printStackTrace();
        }
    }
}