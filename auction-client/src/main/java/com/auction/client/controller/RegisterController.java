package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.enums.UserRole;
import com.auction.common.message.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private ComboBox<UserRole> roleBox;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        // Chỉ cho chọn BIDDER hoặc SELLER (không cho chọn ADMIN)
        roleBox.setItems(FXCollections.observableArrayList(UserRole.BIDDER, UserRole.SELLER));
    }

    @FXML
    public void handleRegister() {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String email = emailField.getText();
        UserRole role = roleBox.getValue();

        if (user.isEmpty() || pass.isEmpty() || email.isEmpty() || role == null) {
            messageLabel.setText("Nhập đủ thông tin!");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            NetworkClient client = NetworkClient.getInstance();
            client.connect();

            RegisterRequest req = new RegisterRequest(user, pass, email, role);
            ClientRequest request = new ClientRequest(Action.REGISTER, req);
            ClientResponse res = client.sendRequest(request);

            if (res.isSuccess()) {
                messageLabel.setText("Đăng ký thành công! Đang chuyển về đăng nhập...");
                messageLabel.setStyle("-fx-text-fill: #2ecc71;");

                // Chuyển về màn Login sau 1.5 giây
                new Thread(() -> {
                    try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(this::goBack);
                }).start();
            } else {
                messageLabel.setText(res.getMessage());
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            }

        } catch (Exception e) {
            messageLabel.setText("Lỗi kết nối!");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    public void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
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