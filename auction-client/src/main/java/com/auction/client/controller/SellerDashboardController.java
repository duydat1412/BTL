package com.auction.client.controller;

import com.auction.common.entity.Auction;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SellerDashboardController {
    @FXML private TableView<Auction> productTable;
    @FXML private TextField titleField;
    @FXML private TextField priceField;
    @FXML
    public void initialize(){
    }
    @FXML
    public void handleAddProduct(){
        String title = titleField.getText();
        String price = priceField.getText();
        System.out.println("Thêm sản phẩm: " + title + " - " + price);
    }


}
