package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.auction.common.message.LoginRequest;
import com.auction.common.message.ClientResponse;
import com.auction.client.Network.NetworkClient;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel; // 👈 THÊM

    // Sửa đoạn code handleLogin trong file LoginController.java của bạn
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
            client.connect("localhost", 1234);
            LoginRequest loginReq = new LoginRequest(user, pass);
            client.sendRequest(loginReq);
            Object response = client.receiveResponse();

            if (response instanceof ClientResponse && ((ClientResponse) response).isSuccess()) {
                Parent root = FXMLLoader.load(getClass().getResource("/view/auction_list.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                errorLabel.setText("Đăng nhập thất bại!");
            }
        } catch (Exception e) {
            errorLabel.setText("Lỗi kết nối Server!");
            e.printStackTrace();
        }
    }
}