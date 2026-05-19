package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminController {

    @FXML
    public void initialize() {
        System.out.println("Admin Dashboard initialized");
    }

    @FXML
    public void handleLogout() {
        System.out.println("Admin logged out");
    }
}
