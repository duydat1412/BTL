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
            strengthLabel.getStyleClass().setAll("status-success");
        } else if (pass.length() >= 6 && (hasUpper || hasDigit)) {
            strengthLabel.setText("Trung bình");
            strengthLabel.getStyleClass().setAll("status-warning");
        } else {
            strengthLabel.setText("Yếu");
            strengthLabel.getStyleClass().setAll("status-error");
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
            messageLabel.getStyleClass().setAll("status-error");
            return;
        }

        NetworkClient client = NetworkClient.getInstance();
        client.connect();

        messageLabel.setText("Đang đăng ký...");
        messageLabel.getStyleClass().setAll("status-info");

        RegisterRequest req = new RegisterRequest(user, pass, email, role);
        ClientRequest request = new ClientRequest(Action.REGISTER, req);

        client.sendRequestAsync(request).thenAccept(res -> Platform.runLater(() -> {
            messageLabel.getStyleClass().setAll(res.isSuccess() ? "status-success" : "status-error");
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
