package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.message.*;
import com.auction.common.enums.UserRole;
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
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            NetworkClient client = NetworkClient.getInstance();
            client.connect();

            ClientResponse res = client.login(user, pass);

            if (res.isSuccess()) {
                // Lưu thông tin user đã đăng nhập
                AuthUserData userData = (AuthUserData) res.getData();
                NetworkClient.getInstance().setCurrentUser(userData);

                // Chuyển hướng theo vai trò
                String fxmlPath;
                if (userData.getRole() == UserRole.SELLER) {
                    fxmlPath = "/view/seller_dashboard.fxml";
                } else {
                    fxmlPath = "/view/auction_list.fxml";
                }

                Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(root);
                String css = getClass().getResource("/css/style.css").toExternalForm();
                scene.getStylesheets().add(css);
                stage.setScene(scene);
            } else {
                errorLabel.setText(res.getMessage());
                errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            }

        } catch (Exception e) {
            errorLabel.setText("Không kết nối được server!");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    public void handleRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/register.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}