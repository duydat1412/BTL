package com.auction.client.controller;

import com.auction.client.model.UserSession;
import com.auction.common.entity.Auction;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.*;

public class SellerDashboardController {
    @FXML private TableView<Auction> productTable;
    @FXML private TextField titleField;
    @FXML private TextField priceField;
    @FXML
    public void intialize(){
        if (UserSession.getInstance().getCurrentUser()==null) {
            System.out.println("Bạn chưa đăng nhập!");
        }
    }
    @FXML
    public void handleAddProduct(){
        String title = titleField.getText();
        String price = priceField.getText();
        System.out.println("Người bán: "+UserSession.getInstance().getCurrentUser());
    }


}
