package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.message.*;
import javafx.application.Platform;
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

        NetworkClient client = NetworkClient.getInstance();
        client.connect();

        errorLabel.setText("Đang đăng nhập...");
        errorLabel.setStyle("-fx-text-fill: gray;");

        client.loginAsync(user, pass).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess()) {
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText(res.getMessage());

                AuthUserData authData = (AuthUserData) res.getData();
                client.setCurrentUser(authData);
                String fxmlFile = "";
                switch (authData.getRole()) {
                    case BIDDER:
                        fxmlFile = "/view/auction_list.fxml";
                        break;
                    case SELLER:
                        fxmlFile = "/view/seller_dashboard.fxml";
                        break;
                    case ADMIN:
                        fxmlFile = "/view/admin.fxml";
                        break;
                }

                if (!fxmlFile.isEmpty()) {
                    try {
                        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
                        usernameField.getScene().setRoot(root);
                    } catch (Exception e) {
                        errorLabel.setText("Lỗi chuyển màn hình!");
                    }
                }
            } else {
                errorLabel.setStyle("-fx-text-fill: #ef4444;");
                errorLabel.setText(res.getMessage());
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> errorLabel.setText("Không kết nối được server!"));
            return null;
        });
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