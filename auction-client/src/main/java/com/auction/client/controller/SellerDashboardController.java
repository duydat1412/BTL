package com.auction.client.controller;

import com.auction.common.entity.Auction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SellerDashboardController {

    @FXML private Label userInfoLabel;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField descField;
    @FXML private Label statusLabel;
    @FXML private ListView<Auction> itemListView;

    @FXML
    public void initialize() {
        // Initialization logic here
        userInfoLabel.setText("Xin chào, Seller");
    }

    @FXML
    public void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) userInfoLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCreateItem() {
        String title = nameField.getText();
        String price = priceField.getText();
        System.out.println("Thêm sản phẩm: " + title + " - " + price);
        statusLabel.setText("Đã thêm sản phẩm thành công!");
    }

    @FXML
    public void handleRefresh() {
        System.out.println("Tải lại danh sách sản phẩm...");
    }
}
