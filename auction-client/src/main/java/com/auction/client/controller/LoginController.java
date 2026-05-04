package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML

    public void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        if (user.isEmpty() || pass.isEmpty()) {

            errorLabel.setText("Không được để trống!");
            return;
        }
        try {
                Parent root = FXMLLoader.load(getClass().getResource("/view/auction_list.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

        } catch (Exception e) {
            errorLabel.setText("Lỗi kết nối Server!");
            e.printStackTrace();
        }
    }
}
