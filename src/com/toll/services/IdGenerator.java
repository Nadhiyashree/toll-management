package com.toll.services;

import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {
    private static final AtomicLong PLAZA_SEQ = new AtomicLong(100);
    private static final AtomicLong LANE_SEQ = new AtomicLong(100);
    private static final AtomicLong TAG_SEQ = new AtomicLong(100000);
    private static final AtomicLong RATE_SEQ = new AtomicLong(100);
    private static final AtomicLong PASS_SEQ = new AtomicLong(500);
    private static final AtomicLong TXN_SEQ = new AtomicLong(1000);
    private static final AtomicLong PMT_SEQ = new AtomicLong(1000);
    private static final AtomicLong DSP_SEQ = new AtomicLong(100);

    public static String nextPlazaId() {
        return "PLZ-" + PLAZA_SEQ.incrementAndGet();
    }

    public static String nextLaneId(String plazaId, int laneNumber) {
        return (plazaId != null ? plazaId : "PLZ") + "-L" + laneNumber;
    }

    public static String nextTagId() {
        return "FT-" + TAG_SEQ.incrementAndGet();
    }

    public static String nextRateId() {
        return "RATE-" + RATE_SEQ.incrementAndGet();
    }

    public static String nextPassId() {
        return "PASS-" + PASS_SEQ.incrementAndGet();
    }

    public static String nextTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + TXN_SEQ.incrementAndGet();
    }

    public static String nextPaymentId() {
        return "PAY-" + System.currentTimeMillis() + "-" + PMT_SEQ.incrementAndGet();
    }

    public static String nextDisputeId() {
        return "DISP-" + DSP_SEQ.incrementAndGet();
    }
}
