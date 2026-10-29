package com.toll.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TollCalculator {
    private static final BigDecimal FIVE = new BigDecimal("5.00");
    private static final BigDecimal HALF = new BigDecimal("0.50");
    private static final BigDecimal TWO = new BigDecimal("2.00");

    public static BigDecimal roundToNearestFive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal divided = amount.divide(FIVE, 0, RoundingMode.HALF_UP);
        return divided.multiply(FIVE).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateReturnRate(BigDecimal singleRate) {
        if (singleRate == null || singleRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal halfRate = singleRate.multiply(HALF);
        return roundToNearestFive(halfRate);
    }

    public static BigDecimal calculateDoubleToll(BigDecimal standardToll) {
        if (standardToll == null || standardToll.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal doubled = standardToll.multiply(TWO);
        return roundToNearestFive(doubled);
    }
}
