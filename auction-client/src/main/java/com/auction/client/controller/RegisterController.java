package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.enums.UserRole;
import com.auction.common.message.*;
import javafx.application.Platform;
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
            return;
        }

        NetworkClient client = NetworkClient.getInstance();
        client.connect();

        messageLabel.setText("Đang đăng ký...");
        messageLabel.setStyle("-fx-text-fill: gray;");

        RegisterRequest req = new RegisterRequest(user, pass, email, role);
        ClientRequest request = new ClientRequest(Action.REGISTER, req);

        client.sendRequestAsync(request).thenAccept(res -> Platform.runLater(() -> {
            messageLabel.setStyle(res.isSuccess() ? "-fx-text-fill: green;" : "-fx-text-fill: #ef4444;");
            messageLabel.setText(res.getMessage());
        })).exceptionally(ex -> {
            Platform.runLater(() -> messageLabel.setText("Lỗi kết nối!"));
            return null;
        });
    }
    @FXML
    public void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
