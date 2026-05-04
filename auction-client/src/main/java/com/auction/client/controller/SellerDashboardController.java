package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.*;

public class SellerDashboardController {
    @FXML
    private TableView<?> productTable;
    @FXML
    private TextField titleField;
    @FXML
    private TextField priceField;

    public void initialize() {
    }

    @FXML
    public void handleAddProduct() {
        String title = titleField.getText();
        String price = priceField.getText();


        System.out.println("Thêm sản phẩm:");
        System.out.println("Tên: " + title);
        System.out.println("Giá: " + price);
    }
}
