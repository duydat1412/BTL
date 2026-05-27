package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.observer.AuctionEventManager;
import com.auction.server.repository.SerializableAuctionRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;

public final class AuctionScheduler {
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(4);
    private static final SerializableAuctionRepository AUCTION_REPOSITORY =
            new SerializableAuctionRepository();
    private static final Map<String, ScheduledFuture<?>> scheduledEndTasks = new ConcurrentHashMap<>();

    private static volatile AuctionEventManager eventManager;

    public static final int ANTI_SNIPE_WINDOW_SECONDS = 30;
    public static final int ANTI_SNIPE_EXTENSION_SECONDS = 30;

    private AuctionScheduler() {}

    public static void setEventManager(AuctionEventManager manager) {
        eventManager = manager;
    }

    public static void scheduleAuctionStart(String auctionId, LocalDateTime startTime) {
        long delayMillis = Duration.between(LocalDateTime.now(), startTime).toMillis();
        if (delayMillis <= 0) {
            startAuctionNow(auctionId);
            return;
        }

        SCHEDULER.schedule(() -> startAuctionNow(auctionId), delayMillis, TimeUnit.MILLISECONDS);
        System.out.println("[Scheduler] Scheduled auction start for " + auctionId);
    }

    public static void scheduleAuctionEnd(String auctionId, long durationMinutes) {
        ScheduledFuture<?> future = SCHEDULER.schedule(() -> {
            finishAuctionNow(auctionId);
        }, durationMinutes, TimeUnit.MINUTES);

        scheduledEndTasks.put(auctionId, future);
        System.out.println("[Scheduler] Scheduled auction end for " + auctionId
                + " after " + durationMinutes + " minute(s).");
    }

    public static void scheduleAuctionEndAt(String auctionId, LocalDateTime endTime) {
        long delayMillis = Duration.between(LocalDateTime.now(), endTime).toMillis();
        if (delayMillis <= 0) {
            finishAuctionNow(auctionId);
            return;
        }

        ScheduledFuture<?> future = SCHEDULER.schedule(() -> finishAuctionNow(auctionId),
                delayMillis, TimeUnit.MILLISECONDS);
        scheduledEndTasks.put(auctionId, future);
        System.out.println("[Scheduler] Scheduled auction end for " + auctionId + " at absolute time.");
    }

    public static boolean extendAuctionEnd(String auctionId, long extraSeconds) {
        ScheduledFuture<?> existing = scheduledEndTasks.remove(auctionId);
        if (existing != null && !existing.isDone()) {
            existing.cancel(false);
        }

        Auction auction = AUCTION_REPOSITORY.findById(auctionId);
        if (auction == null) return false;

        LocalDateTime newEnd = auction.getEndTime().plusSeconds(extraSeconds);
        auction.setEndTime(newEnd);
        AUCTION_REPOSITORY.update(auction);

        scheduleAuctionEndAt(auctionId, newEnd);
        System.out.println("[Scheduler] Extended auction " + auctionId
                + " by " + extraSeconds + "s, new end: " + newEnd);
        return true;
    }

    private static void startAuctionNow(String auctionId) {
        try {
            Auction auction = AUCTION_REPOSITORY.findById(auctionId);
            if (auction != null && auction.getStatus() == AuctionStatus.OPEN) {
                AuctionStatus oldStatus = auction.getStatus();
                auction.setStatus(AuctionStatus.RUNNING);
                AUCTION_REPOSITORY.update(auction);
                if (eventManager != null) {
                    eventManager.notifyStatusChanged(auction, oldStatus, AuctionStatus.RUNNING);
                }
                System.out.println("[Scheduler] Auction " + auctionId + " started automatically.");
            }
        } catch (Exception e) {
            System.err.println("[Scheduler] Error while starting auction: " + e.getMessage());
        }
    }

    private static void finishAuctionNow(String auctionId) {
        try {
            scheduledEndTasks.remove(auctionId);
            Auction auction = AUCTION_REPOSITORY.findById(auctionId);
            if (auction != null && auction.getStatus() == AuctionStatus.RUNNING) {
                AuctionStatus oldStatus = auction.getStatus();
                auction.setStatus(AuctionStatus.FINISHED);
                AUCTION_REPOSITORY.update(auction);
                if (eventManager != null) {
                    eventManager.notifyStatusChanged(auction, oldStatus, AuctionStatus.FINISHED);
                    eventManager.notifyAuctionEnded(auction);
                }
                System.out.println("[Scheduler] Auction " + auctionId + " finished automatically.");
            }
        } catch (Exception e) {
            System.err.println("[Scheduler] Error while ending auction: " + e.getMessage());
        }
    }
}
