package com.options.entities;

public class DownloadableOption {

    private String symbol;
    private Integer min_days_to_expiration;

    public DownloadableOption() {}

    public DownloadableOption(String symbol, Integer min_days_to_expiration) {
        this.symbol = symbol;
        this.min_days_to_expiration = min_days_to_expiration;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getMin_days_to_expiration() {
        return min_days_to_expiration;
    }

    public void setMin_days_to_expiration(Integer min_days_to_expiration) {
        this.min_days_to_expiration = min_days_to_expiration;
    }

    @Override
    public String toString() {
        return "DownloadableOption{" +
                "symbol='" + symbol + '\'' +
                ", min_days_to_expiration='" + min_days_to_expiration + '\'' +
                '}';
    }
}
