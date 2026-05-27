package com.auction.client.util;

import com.auction.client.network.NetworkClient;
import com.auction.client.controller.LoginController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;

public class BanHandler {
    public static void handleBan(Scene scene, String reason) {
        Platform.runLater(() -> {
            // Ngắt kết nối mạng ngay lập tức
            NetworkClient.getInstance().disconnect();

            // Hiển thị Alert Dialog báo lỗi
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Tài khoản bị cấm");
            alert.setHeaderText("Bạn đã bị quản trị viên khóa tài khoản!");
            alert.setContentText("Lý do: " + (reason != null && !reason.trim().isEmpty() ? reason : "Không có lý do cụ thể."));

            // Áp dụng CSS dark theme cho Dialog
            DialogPane dialogPane = alert.getDialogPane();
            if (dialogPane != null) {
                try {
                    String cssPath = BanHandler.class.getResource("/CSS/style.css").toExternalForm();
                    dialogPane.getStylesheets().add(cssPath);
                    dialogPane.getStyleClass().add("ban-alert-dialog");
                } catch (Exception e) {
                    System.err.println("Không thể nạp style cho Alert Dialog: " + e.getMessage());
                }
            }

            // Chờ người dùng click xác nhận
            alert.showAndWait();

            // Quay về màn hình Login kèm lý do ban
            try {
                FXMLLoader loader = new FXMLLoader(BanHandler.class.getResource("/view/login.fxml"));
                Parent root = loader.load();
                LoginController controller = loader.getController();
                if (controller != null) {
                    controller.setBanReason(reason);
                }
                if (scene != null) {
                    scene.setRoot(root);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
