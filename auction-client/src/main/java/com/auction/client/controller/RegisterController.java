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
        roleBox.setItems(FXCollections.observableArrayList(UserRole.values()));
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

        try {
            NetworkClient client = NetworkClient.getInstance();
            client.connect();

            RegisterRequest req = new RegisterRequest(user, pass, email, role);
            ClientRequest request = new ClientRequest(Action.REGISTER, req);
            ClientResponse res = client.sendRequest(request);

            messageLabel.setText(res.getMessage());

        } catch (Exception e) {
            messageLabel.setText("Lỗi kết nối!");
        }
    }
    @FXML
    public void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}