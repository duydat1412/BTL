package com.auction.client.controller;

import com.auction.client.network.NetworkClient;
import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.entity.Item;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.message.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.auction.client.util.NotificationToast;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuctionDetailController {

    @FXML private VBox notificationCard;
    @FXML private Label notificationMessageLabel;
    @FXML private Label notificationIconLabel;
    @FXML private Label notificationTitleLabel;
    @FXML private HBox timerBox;

    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label timeLabel;
    @FXML private TextField bidAmountField;
    @FXML private Label descLabel;
    @FXML private Button placeBidBtn;

    @FXML private ListView<String> bidHistoryView;
    @FXML private Label bidHistoryStatus;

    @FXML private TextField maxBidField;
    @FXML private TextField incrementField;
    @FXML private Button autoBidBtn;
    @FXML private Label balanceLabel;
    @FXML private LineChart<Number, Number> priceChart;

    private XYChart.Series<Number, Number> allBidsSeries;
    private XYChart.Series<Number, Number> myBidsSeries;
    private Auction currentAuction;
    private Item currentItem;
    private NetworkClient.PushListener pushListener;
    private Timeline countdownTimeline;
    private boolean autoBidEnabled = false;

    public void setData(Auction auction, Item item) {
        this.currentAuction = auction;
        this.currentItem = item;

        if (auction == null && item == null) {
            nameLabel.setText("Không có dữ liệu");
            return;
        }

        String title = auction != null ? auction.getTitle() : item.getName();
        double price = auction != null ? auction.getCurrentPrice() : item.getStartingPrice();
        String desc = item != null && item.getDescription() != null ? item.getDescription() : "Không có mô tả chi tiết.";
        if (item != null) desc = item.getDetailedDescription();

        nameLabel.setText(title);
        priceLabel.setText(String.format("%,.0f VNĐ", price));
        if (descLabel != null) {
            descLabel.setText(desc);
        }

        // Init price chart
        initPriceChart();

        // Load balance
        loadBalance();

        // Fetch fresh auction data từ server để check status mới nhất
        loadFreshAuctionData();

        // Register push listener
        registerPushListener();

        // Callback khi bị ban
        NetworkClient.getInstance().setOnBannedCallback(reason -> {
            com.auction.client.util.BanHandler.handleBan(nameLabel.getScene(), reason);
        });
    }

    private static final String[] STATUS_CLASSES = {
            "status-success", "status-error", "status-warning", "status-info",
            "text-up", "text-down", "text-warning"
    };

    private void setTimeLabelStyle(String... classes) {
        timeLabel.getStyleClass().removeAll(STATUS_CLASSES);
        timeLabel.getStyleClass().addAll(classes);
    }
    private void initPriceChart() {
        if (priceChart == null) return;

        allBidsSeries = new XYChart.Series<>();
        allBidsSeries.setName("Tất cả lượt đặt");
        myBidsSeries = new XYChart.Series<>();
        myBidsSeries.setName("Lượt đặt của tôi");

        priceChart.getData().clear();
        priceChart.getData().add(allBidsSeries);
        priceChart.getData().add(myBidsSeries);

        // Style series via lookup after render
        priceChart.setAnimated(false);
    }

    private void loadFreshAuctionData() {
        if (currentAuction == null) return;
        String auctionId = currentAuction.getId();
        NetworkClient.getInstance().sendRequestAsync(
                new ClientRequest(Action.GET_AUCTION, auctionId)
        ).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() instanceof Auction fresh) {
                currentAuction = fresh;
                currentAuction.setCurrentPrice(fresh.getCurrentPrice());
                if (fresh.getEndTime() != null) currentAuction.setEndTime(fresh.getEndTime());
                priceLabel.setText(String.format("%,.0f VNĐ", fresh.getCurrentPrice()));

                if (fresh.getStatus() == AuctionStatus.CANCELED) {
                    if (timerBox != null) {
                        timerBox.setVisible(false);
                        timerBox.setManaged(false);
                    }
                    disableAllControls();
                    loadBidHistory();
                    return;
                }
                if (fresh.getStatus() == AuctionStatus.FINISHED) {
                    if (timerBox != null) {
                        timerBox.setVisible(false);
                        timerBox.setManaged(false);
                    }
                    disableAllControls();
                    loadBidHistory();
                    return;
                }
                // Auction còn hoạt động — start countdown + load bid history
                if (timerBox != null) {
                    timerBox.setVisible(true);
                    timerBox.setManaged(true);
                }
                startCountdown();
                loadBidHistory();
            } else {
                // Không fetch được — start với dữ liệu hiện tại
                startCountdown();
                loadBidHistory();
            }
        }));
    }

    private void disableAllControls() {
        placeBidBtn.setDisable(true);
        bidAmountField.setDisable(true);
        maxBidField.setDisable(true);
        incrementField.setDisable(true);
        autoBidBtn.setDisable(true);
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    private void startCountdown() {
        if (currentAuction == null || currentAuction.getEndTime() == null) return;
        if (currentAuction.getStatus() == AuctionStatus.CANCELED
                || currentAuction.getStatus() == AuctionStatus.FINISHED) return;

        if (timerBox != null) {
            timerBox.setVisible(true);
            timerBox.setManaged(true);
        }

        if (countdownTimeline != null) countdownTimeline.stop();

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateCountdown()));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void updateCountdown() {
        if (currentAuction == null || currentAuction.getEndTime() == null) return;

        LocalDateTime end = currentAuction.getEndTime();
        java.time.Duration remaining = java.time.Duration.between(LocalDateTime.now(), end);

        if (remaining.isNegative() || remaining.isZero()) {
            // Không tự disable — chờ server gửi AUCTION_ENDED (anti-sniping có thể gia hạn)
            timeLabel.setText("Đang chờ server xác nhận kết thúc...");
            setTimeLabelStyle("status-warning");
            return;
        }

        long h = remaining.toHours();
        long m = remaining.toMinutes() % 60;
        long s = remaining.getSeconds() % 60;
        timeLabel.setText(String.format("Còn lại: %02d:%02d:%02d", h, m, s));

        // Color: xanh (>1h), vàng (<1h), đỏ (<5 phút)
        if (remaining.toHours() >= 1) {
            setTimeLabelStyle("text-up");
        } else if (remaining.toMinutes() >= 5) {
            setTimeLabelStyle("text-warning");
        } else {
            setTimeLabelStyle("text-down");
        }
    }

    private void loadBidHistory() {
        if (currentAuction == null) return;
        bidHistoryStatus.setText("Đang tải...");
        ClientRequest req = new ClientRequest(Action.GET_BID_HISTORY, new GetBidHistoryRequest(currentAuction.getId()));
        NetworkClient.getInstance().sendRequestAsync(req).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                List<?> rawList = (List<?>) res.getData();
                List<BidTransaction> bids = rawList.stream()
                        .filter(BidTransaction.class::isInstance)
                        .map(BidTransaction.class::cast)
                        .toList();
                updateBidHistoryView(bids);
                bidHistoryStatus.setText(bids.size() + " lượt đặt giá");
            } else {
                bidHistoryStatus.setText("Không có dữ liệu");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> bidHistoryStatus.setText("Lỗi tải!"));
            return null;
        });
    }

    /** Load bid history không block — dùng cho push listener để tránh requestLock contention */
    private void loadBidHistoryAsync() {
        if (currentAuction == null) return;
        ClientRequest req = new ClientRequest(Action.GET_BID_HISTORY, new GetBidHistoryRequest(currentAuction.getId()));
        NetworkClient.getInstance().sendRequestAsync(req).thenAccept(res -> {
            if (res.isSuccess() && res.getData() != null) {
                List<?> rawList = (List<?>) res.getData();
                List<BidTransaction> bids = rawList.stream()
                        .filter(BidTransaction.class::isInstance)
                        .map(BidTransaction.class::cast)
                        .toList();
                Platform.runLater(() -> {
                    updateBidHistoryView(bids);
                    bidHistoryStatus.setText(bids.size() + " lượt đặt giá");
                });
            }
        });
    }

    private void updateBidHistoryView(List<BidTransaction> bids) {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        String currentUserId = user != null ? user.getUserId() : "";

        bidHistoryView.getItems().clear();

        // Update price chart
        if (priceChart != null) {
            allBidsSeries.getData().clear();
            myBidsSeries.getData().clear();
            // chronological order (oldest first)
            for (int i = 0; i < bids.size(); i++) {
                BidTransaction bid = bids.get(i);
                double y = bid.getBidAmount();
                allBidsSeries.getData().add(new XYChart.Data<>(i + 1, y));
            }
            // my bids — same X indices
            for (int i = 0; i < bids.size(); i++) {
                BidTransaction bid = bids.get(i);
                if (bid.getBidderId().equals(currentUserId)) {
                    myBidsSeries.getData().add(new XYChart.Data<>(i + 1, bid.getBidAmount()));
                }
            }
        }
        int seq = bids.size();
        for (int i = bids.size() - 1; i >= 0; i--) {
            BidTransaction bid = bids.get(i);
            String formatted = String.format("%d. %s: %,.0f VNĐ - %s",
                    seq--,
                    bid.getBidderId(),
                    bid.getBidAmount(),
                    bid.getBidTime() != null ? bid.getBidTime().format(DateTimeFormatter.ofPattern("dd/MM HH:mm:ss")) : "");
            bidHistoryView.getItems().add(formatted);
        }

        // Highlight current user's bids using cell factory
        bidHistoryView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Check if this bid is by current user (by checking bidderId in text)
                    if (currentUserId != null && !currentUserId.isEmpty() && item.contains(currentUserId)) {
                        if (!getStyleClass().contains("bid-highlight")) {
                            getStyleClass().add("bid-highlight");
                        }
                    } else {
                        getStyleClass().remove("bid-highlight");
                    }
                }
            }
        });

        // Show detailed notification card if auction is completed or canceled
        if (notificationCard != null && notificationMessageLabel != null && currentAuction != null) {
            if (currentAuction.getStatus() == AuctionStatus.FINISHED) {
                String winner = currentAuction.getHighestBidderId();
                double finalPrice = currentAuction.getCurrentPrice();
                
                String icon = "🏆";
                String title = "Kết quả đấu giá";
                String borderStyle = "-accent-yellow";
                String titleStyle = "-fx-text-fill: -accent-yellow;";
                
                String message;
                if (winner != null && !winner.trim().isEmpty()) {
                    message = String.format("Phiên đấu giá đã kết thúc thành công!\nNgười thắng cuộc là \"%s\" với mức giá %,.0f VNĐ.", 
                            winner, finalPrice);
                } else if (!bids.isEmpty()) {
                    BidTransaction winnerBid = bids.get(bids.size() - 1);
                    message = String.format("Phiên đấu giá đã kết thúc thành công!\nNgười thắng cuộc là \"%s\" với mức giá %,.0f VNĐ.", 
                            winnerBid.getBidderId(), winnerBid.getBidAmount());
                } else {
                    message = "Phiên đấu giá đã kết thúc nhưng không có lượt đặt giá nào hợp lệ.";
                    icon = "🏁";
                    borderStyle = "-muted";
                    titleStyle = "-fx-text-fill: -muted;";
                }
                showNotification(icon, title, message, borderStyle, titleStyle);
            } else if (currentAuction.getStatus() == AuctionStatus.CANCELED) {
                showNotification("🚫", "Thông báo hủy", "Phiên đấu giá này đã bị hủy bởi quản trị viên.", "-muted", "-fx-text-fill: -muted;");
            } else {
                String currentTitle = notificationTitleLabel != null ? notificationTitleLabel.getText() : "";
                if (!currentTitle.startsWith("Đặt giá")) {
                    closeNotification();
                }
            }
        }
    }

    private void loadBalance() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null || balanceLabel == null) return;
        ClientRequest req = new ClientRequest(Action.GET_BALANCE, user.getUserId());
        NetworkClient.getInstance().sendRequestAsync(req).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess() && res.getData() != null) {
                double balance = (double) res.getData();
                balanceLabel.setText("Số dư: " + String.format("%,.0f", balance) + " VNĐ");
                // Cập nhật balance trong AuthUserData để dùng cho client-side check
                user.setBalance(balance);
            }
        }));
    }

    private void registerPushListener() {
        pushListener = pushMsg -> {
            // Cập nhật endTime + giá từ push (quan trọng cho anti-sniping và auto-bid)
            if ((pushMsg.getType() == ServerPushMessage.PushType.NEW_BID
                    || pushMsg.getType() == ServerPushMessage.PushType.PRICE_UPDATE)
                    && pushMsg.getData() instanceof Auction updatedAuction) {

                if (currentAuction != null
                        && updatedAuction.getId().equals(currentAuction.getId())) {
                    Platform.runLater(() -> {
                        currentAuction.setCurrentPrice(updatedAuction.getCurrentPrice());
                        if (updatedAuction.getEndTime() != null) {
                            currentAuction.setEndTime(updatedAuction.getEndTime());
                        }
                        priceLabel.setText(String.format("%,.0f VNĐ", updatedAuction.getCurrentPrice()));
                        // Load bid history bất đồng bộ — không block UI
                        loadBidHistoryAsync();
                    });
                }
            } else if (pushMsg.getType() == ServerPushMessage.PushType.AUCTION_ENDED) {
                if (pushMsg.getData() instanceof Auction endedAuction
                        && currentAuction != null
                        && endedAuction.getId().equals(currentAuction.getId())) {
                    Platform.runLater(() -> {
                        currentAuction.setStatus(endedAuction.getStatus());
                        if (timerBox != null) {
                            timerBox.setVisible(false);
                            timerBox.setManaged(false);
                        }
                        disableAllControls();
                        loadBidHistory();
                        loadBalance();
                        if (pushMsg.getMessage() != null) {
                            NotificationToast.show(nameLabel.getScene().getWindow(), pushMsg.getMessage(), false);
                        }
                    });
                }
            }
        };
        NetworkClient.getInstance().addPushListener(pushListener);
    }

    @FXML
    public void handlePlaceBid() {
        String amountStr = bidAmountField.getText();

        if (amountStr.isEmpty()) {
            showStatus("Chưa nhập giá!", true);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String auctionId = currentAuction != null ? currentAuction.getId() : (currentItem != null ? currentItem.getId() : "");

            AuthUserData user = NetworkClient.getInstance().getCurrentUser();
            if (user == null) {
                showStatus("Vui lòng đăng nhập lại!", true);
                return;
            }

            // Client-side balance check
            double balance = user.getBalance();
            if (balance < amount) {
                showStatus("Số dư không đủ! Số dư: " + String.format("%,.0f", balance) + " VNĐ", true);
                return;
            }

            PlaceBidRequest req = new PlaceBidRequest(auctionId, user.getUserId(), amount, false);
            ClientRequest request = new ClientRequest(Action.PLACE_BID, req);

            showStatus("Đang xử lý...", false);
            NetworkClient.getInstance().sendRequestAsync(request).thenAccept(res -> Platform.runLater(() -> {
                if (res.isSuccess()) {
                    showStatus("Đặt giá thành công: " + String.format("%,.0f", amount) + " VNĐ", false);
                    bidAmountField.clear();
                    loadBidHistory();
                } else {
                    showStatus("Lỗi: " + res.getMessage(), true);
                }
            })).exceptionally(ex -> {
                Platform.runLater(() -> showStatus("Lỗi kết nối server!", true));
                return null;
            });

        } catch (NumberFormatException ex) {
            showStatus("Giá trị không hợp lệ!", true);
        }
    }

    @FXML
    public void handleAutoBid() {
        if (autoBidEnabled) {
            // Disable auto-bid
            removeAutoBid();
        } else {
            // Enable auto-bid
            enableAutoBid();
        }
    }

    private void enableAutoBid() {
        String maxBidStr = maxBidField.getText();
        if (maxBidStr.isEmpty()) {
            showStatus("Nhập giá tối đa cho auto-bid!", true);
            return;
        }

        String incStr = incrementField.getText();
        if (incStr.isEmpty()) {
            showStatus("Nhập bước tăng giá cho auto-bid!", true);
            return;
        }

        try {
            double maxBid = Double.parseDouble(maxBidStr);
            double increment = Double.parseDouble(incStr);

            if (increment <= 0) {
                showStatus("Bước tăng phải lớn hơn 0!", true);
                return;
            }

            AuthUserData user = NetworkClient.getInstance().getCurrentUser();
            if (user == null) {
                showStatus("Vui lòng đăng nhập lại!", true);
                return;
            }

            // Client-side balance check
            double balance = user.getBalance();
            if (balance < maxBid) {
                showStatus("Số dư không đủ cho auto-bid! Số dư: " + String.format("%,.0f", balance) + " VNĐ", true);
                return;
            }

            String auctionId = currentAuction != null ? currentAuction.getId() : "";
            RegisterAutoBidRequest req = new RegisterAutoBidRequest(auctionId, user.getUserId(), maxBid, increment);
            ClientRequest request = new ClientRequest(Action.REGISTER_AUTO_BID, req);

            showStatus("Đang đăng ký auto-bid...", false);
            NetworkClient.getInstance().sendRequestAsync(request).thenAccept(res -> Platform.runLater(() -> {
                if (res.isSuccess()) {
                    autoBidEnabled = true;
                    autoBidBtn.setText("Hủy auto-bid");
                    autoBidBtn.getStyleClass().setAll("auto-bid-on");
                    maxBidField.setDisable(true);
                    incrementField.setDisable(true);
                    showStatus("Auto-bid đã bật!", false);
                } else {
                    showStatus("Lỗi: " + res.getMessage(), true);
                }
            })).exceptionally(ex -> {
                Platform.runLater(() -> showStatus("Lỗi kết nối server!", true));
                return null;
            });

        } catch (NumberFormatException ex) {
            showStatus("Giá tối đa không hợp lệ!", true);
        }
    }

    private void removeAutoBid() {
        AuthUserData user = NetworkClient.getInstance().getCurrentUser();
        if (user == null) return;

        String auctionId = currentAuction != null ? currentAuction.getId() : "";
        RemoveAutoBidRequest req = new RemoveAutoBidRequest(auctionId, user.getUserId());
        ClientRequest request = new ClientRequest(Action.REMOVE_AUTO_BID, req);

        NetworkClient.getInstance().sendRequestAsync(request).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess()) {
                autoBidEnabled = false;
                autoBidBtn.setText("Bật auto-bid");
                autoBidBtn.getStyleClass().setAll("btn-primary");
                maxBidField.setDisable(false);
                incrementField.setDisable(false);
                showStatus("Đã hủy auto-bid", false);
            } else {
                showStatus("Lỗi: " + res.getMessage(), true);
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> showStatus("Lỗi kết nối server!", true));
            return null;
        });
    }

    @FXML
    public void closeNotification() {
        if (notificationCard != null && notificationCard.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), notificationCard);
            fadeOut.setToValue(0.0);
            
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), notificationCard);
            slideOut.setToY(-15.0);
            
            ParallelTransition closeAnim = new ParallelTransition(fadeOut, slideOut);
            closeAnim.setInterpolator(Interpolator.EASE_IN);
            closeAnim.setOnFinished(e -> {
                notificationCard.setVisible(false);
                notificationCard.setManaged(false);
                notificationCard.setTranslateY(0); // Reset translation
            });
            closeAnim.play();
        }
    }

    private void showNotification(String icon, String title, String message, String borderStyle, String titleStyle) {
        if (notificationCard == null || notificationMessageLabel == null) return;
        
        boolean wasVisible = notificationCard.isVisible();
        
        if (notificationIconLabel != null) notificationIconLabel.setText(icon);
        if (notificationTitleLabel != null) {
            notificationTitleLabel.setText(title);
            notificationTitleLabel.setStyle(titleStyle);
        }
        notificationMessageLabel.setText(message);
        notificationCard.setStyle("-fx-background-color: -surface-dark-el; -fx-border-color: " + borderStyle + "; -fx-border-width: 1px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 15px;");
        
        if (!wasVisible) {
            notificationCard.setVisible(true);
            notificationCard.setManaged(true);
            
            // Trạng thái bắt đầu của animation
            notificationCard.setOpacity(0.0);
            notificationCard.setTranslateY(-15);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationCard);
            fadeIn.setToValue(1.0);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notificationCard);
            slideIn.setToY(0.0);
            
            ParallelTransition showAnim = new ParallelTransition(fadeIn, slideIn);
            showAnim.setInterpolator(Interpolator.EASE_OUT);
            showAnim.play();
        } else {
            // Pulse nháy nhẹ khi cập nhật thông tin mới trên thẻ đang mở
            FadeTransition pulse = new FadeTransition(Duration.millis(150), notificationCard);
            pulse.setFromValue(1.0);
            pulse.setToValue(0.5);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(2);
            pulse.play();
        }
    }

    private void showStatus(String msg, boolean isError) {
        if (isError) {
            showNotification("❌", "Đặt giá thất bại", msg, "-semantic-down", "-fx-text-fill: -semantic-down;");
        } else {
            showNotification("✅", "Đặt giá thành công!", msg, "-semantic-up", "-fx-text-fill: -semantic-up;");
        }
    }

    @FXML
    public void goBack() {
        if (countdownTimeline != null) countdownTimeline.stop();
        if (pushListener != null) {
            NetworkClient.getInstance().removePushListener(pushListener);
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/auction_list.fxml"));
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//1