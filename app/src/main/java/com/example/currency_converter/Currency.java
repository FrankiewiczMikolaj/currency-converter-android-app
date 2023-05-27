package com.example.currency_converter;

import androidx.annotation.NonNull;

public class Currency {
    private final String currencyCode;
    private final int currencyFlagDrawableId;
    private final double  currencyMid;

    public Currency(String name, int flagResource, double currencyMid) {
        this.currencyCode = name;
        this.currencyFlagDrawableId = flagResource;
        this.currencyMid = currencyMid;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getCurrencyMid(){return String.valueOf(currencyMid);}

    public int getCurrencyFlagDrawableId() {
        return currencyFlagDrawableId;
    }
    @NonNull
    @Override
    public String toString() {
        return currencyCode;
    }
}