package com.swiggy.wallet.enums;

public enum Currency {

    INR(1.0),
    USD(83.10),
    EUR(89.04);

    private final double conversionFactor;

    Currency(double conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    public double getConversionFactor() {
        return conversionFactor;
    }}
