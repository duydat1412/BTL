package com.auction.server.service;

import java.time.LocalDateTime;
import com.auction.common.entity.Auction;
import com.auction.common.enums.*;

import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionScheduler {
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public AuctionScheduler(Auction auction){
        long delay=auction.getEndTime().toInstant(ZoneOffset.ofHours(7)).toEpochMilli()-System.currentTimeMillis();
        scheduler.schedule(() -> auction.setStatus(AuctionStatus.FINISHED), delay, TimeUnit.MILLISECONDS);
    }
}
