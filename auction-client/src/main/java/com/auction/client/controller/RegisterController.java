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
    @FXML private TextField visiblePasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField visibleConfirmPasswordField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private TextField emailField;
    @FXML private ComboBox<UserRole> roleBox;
    @FXML private Label messageLabel;
    @FXML private Label strengthLabel;

    @FXML
    public void initialize() {
        roleBox.setItems(FXCollections.observableArrayList(UserRole.BIDDER, UserRole.SELLER));

        // Sync visible text fields with password fields
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visibleConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        // Listen for password changes to update strength indicator
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> updateStrengthIndicator(newVal));
        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> updateStrengthIndicator(newVal));
    }

    private void updateStrengthIndicator(String pass) {
        if (pass == null || pass.isEmpty()) {
            strengthLabel.setVisible(false);
            strengthLabel.setManaged(false);
            return;
        }
        strengthLabel.setVisible(true);
        strengthLabel.setManaged(true);

        boolean hasUpper = pass.matches(".*[A-Z].*");
        boolean hasDigit = pass.matches(".*\\d.*");
        boolean hasSpecial = pass.matches(".*[!@#$%^&*(),.?\":{}|<>_+\\-=\\[\\];'\\\\].*");

        if (pass.length() >= 8 && hasUpper && hasDigit && hasSpecial) {
            strengthLabel.setText("Mạnh");
            strengthLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
        } else if (pass.length() >= 6 && (hasUpper || hasDigit)) {
            strengthLabel.setText("Trung bình");
            strengthLabel.setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;");
        } else {
            strengthLabel.setText("Yếu");
            strengthLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        }
    }

    @FXML
    public void togglePasswordVisibility() {
        boolean selected = showPasswordCheckBox.isSelected();
        passwordField.setVisible(!selected);
        passwordField.setManaged(!selected);
        confirmPasswordField.setVisible(!selected);
        confirmPasswordField.setManaged(!selected);
        visiblePasswordField.setVisible(selected);
        visiblePasswordField.setManaged(selected);
        visibleConfirmPasswordField.setVisible(selected);
        visibleConfirmPasswordField.setManaged(selected);
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

        String confirm = confirmPasswordField.getText();
        if (!pass.equals(confirm)) {
            messageLabel.setText("Mật khẩu không khớp!");
            messageLabel.setStyle("-fx-text-fill: #ef4444;");
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
