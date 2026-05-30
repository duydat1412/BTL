package com.auction.client.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Utility class to show toast notifications in the top-right corner.
 */
public class NotificationToast {

    /**
     * Show a toast notification for 3 seconds.
     *
     * @param window  the parent window to anchor the toast
     * @param message the message to display
     * @param isError true for error styling, false for info
     */
    public static void show(Window window, String message, boolean isError) {
        Platform.runLater(() -> {
            Popup popup = new Popup();
            popup.setAutoFix(true);

            Label msgLabel = new Label(message);
            msgLabel.setWrapText(true);
            msgLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

            StackPane content = new StackPane(msgLabel);
            content.setStyle(isError
                    ? "-fx-background-color: #ef4444; -fx-padding: 15 25; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 4);"
                    : "-fx-background-color: #22c55e; -fx-padding: 15 25; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 4);");

            popup.getContent().add(content);

            // Position at top-right of window
            popup.setOnShown(e -> {
                popup.setX(window.getX() + window.getWidth() - content.getWidth() - 20);
                popup.setY(window.getY() + 20);
            });

            popup.show(window);

            // Auto-hide after 3 seconds
            Timeline hideTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> popup.hide()));
            hideTimeline.setCycleCount(1);
            hideTimeline.play();
        });
    }
}
