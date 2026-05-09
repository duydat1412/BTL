package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.message.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
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

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Không được để trống!");
            return;
        }

        try {
            NetworkClient client = NetworkClient.getInstance();
            client.connect();
            ClientResponse res = client.login(user, pass);

            errorLabel.setText(res.getMessage());

        } catch (Exception e) {
            errorLabel.setText("Không kết nối được server!");
        }
    }

    @FXML
    public void handleRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/register.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}